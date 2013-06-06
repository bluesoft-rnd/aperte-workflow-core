package pl.net.bluesoft.awf.ext.droolsstep;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.rule.builder.dialect.mvel.MVELDialectConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;

import pl.net.bluesoft.util.lang.Formats;
import pl.net.bluesoft.util.lang.Mapcar;

public class DroolsUtils {
	private static final Logger log = Logger.getLogger(DroolsUtils.class.getName());

    public static class DroolsResource {
        private String ruleUrl;
        private InputStream ruleStream;

        public DroolsResource(String ruleUrl) {
            this.ruleUrl = ruleUrl;
        }

        public DroolsResource(String ruleUrl, InputStream ruleStream) {
            this.ruleUrl = ruleUrl;
            this.ruleStream = ruleStream;
        }

        public String getRuleUrl() {
            return ruleUrl;
        }

        public InputStream getRuleStream() {
            return ruleStream;
        }
    }

	public static List processRules(List facts, String... rulesUrl) {
		Long timer = System.currentTimeMillis();
		List res = processRules("result", facts, rulesUrl);
		log.fine("Timing [processing drools rules]. Duration: " + (System.currentTimeMillis() - timer) + "ms");
		return res;
	}

	public static List processRules(String rulesUrl, List objects) {
		return processRules("result", objects, rulesUrl);
	}

	public static List processRulesStafeful(String rulesUrl, List objects) {
		final List res = new ArrayList();

		HashMap map = new HashMap();
		map.put("result", res);
		processCachedRulesAndReturnFacts(objects, map, rulesUrl);
		return res;
	}

    public static List processRules(List facts, Map<String, Object> globals, DroolsResource... droolsResources) {
        Long timer = System.currentTimeMillis();
        StatefulKnowledgeSession knowledgeSession = getCachedSession(droolsResources);
        try {
            log.fine("Timing [getting session]. Duration: " + (System.currentTimeMillis() - timer) + "ms");
            return processRulesWithSession(facts, globals, knowledgeSession);
        }
        catch (RuntimeException e) {
            //clear rule cache
            PACK_CACHE.clear();
            BASE_CACHE.clear();
            throw e;
        }
        finally {
            knowledgeSession.dispose();
            log.fine("Timing [process rules]. Duration: " + (System.currentTimeMillis() - timer) + "ms");
        }
    }

    public static DroolsResource[] transformRuleUrls(String... rulesUrls) {
        DroolsResource[] resources = new DroolsResource[rulesUrls.length];
        for (int i = 0; i < rulesUrls.length; ++i) {
            resources[i] = new DroolsResource(rulesUrls[i]);
        }
        return resources;
    }

    public static List processRules(List facts, Map<String, Object> globals, String... rulesUrls) {
        return processRules(facts, globals, transformRuleUrls(rulesUrls));
    }

	public static List processRulesWithSession(List facts, Map<String, Object> globals, StatefulKnowledgeSession session) {
		int cnt = 0;
		try {
			while (cnt++ < 10) {
				try {
					for (Map.Entry<String, Object> e : globals.entrySet()) {
						session.setGlobal(e.getKey(), e.getValue());
					}
					for (Object o : facts) {
						synchronized (session) {
							session.insert(o);
						}
                    }
					session.fireAllRules();
					return new ArrayList(session.getObjects());
				}
				catch (ConcurrentModificationException e) {
					session.dispose();
				}
			}
			throw new RuntimeException("drools failed to initialize session 10 times in a row!");
		}
		finally {
			session.dispose();
		}
	}


	private static List processRules(final String resultName, List objects, String... rulesUrls) {
		final List result = new ArrayList();
		HashMap map = new HashMap();
		map.put(resultName, result);
		processRules(objects, map, rulesUrls);
		return result;
	}

	public static Collection processCachedRulesAndReturnFacts(List objects, Map<String, Object> globals, String rulesUrl) {
		StatefulKnowledgeSession session = getCachedSession(rulesUrl);
		try {
			if (session != null) {
                for (Map.Entry<String, Object> e : globals.entrySet()) {
                    session.setGlobal(e.getKey(), e.getValue());
                }
                for (Object o : objects)
                    synchronized (session) {
                        session.insert(o);
                    }
                session.fireAllRules();
                return session.getObjects();
			} else {
				return null;
			}
		}
		finally {
			if (session != null) session.dispose();
		}
	}

	private static final Map<String, KnowledgeBase> BASE_CACHE = new HashMap();
	private static final Map<String, Collection<KnowledgePackage>> PACK_CACHE = new HashMap();
	private static final Map<String, Long> VALIDITY_CACHE = new HashMap();
	private static final Map<String, Long> PACK_VALIDITY_CACHE = new HashMap();
	private static final long VALIDITY_TIME = 15 * 60 * 1000; 
	private static final long PACK_VALIDITY_TIME = 60 * 60 * 1000; //1 godzina

    public static synchronized StatefulKnowledgeSession getCachedSession(DroolsResource... droolsResources) {
        KnowledgeBase kb = getCachedKnowledgeBase(droolsResources);
        long t = System.currentTimeMillis();
        try {
            return initSession(kb);
        }
        finally {
            log.fine("initSession took: " + (System.currentTimeMillis() - t));
        }
    }

	public static synchronized StatefulKnowledgeSession getCachedSession(String... rulesUrls) {
        return getCachedSession(transformRuleUrls(rulesUrls));
	}

    private static KnowledgeBase getCachedKnowledgeBase(DroolsResource... droolsResources) {
        List<DroolsResource> resources = Arrays.asList(droolsResources);
        List<String> rules = new Mapcar<DroolsResource, String>(resources) {
            @Override
            public String lambda(DroolsResource x) {
                return x.getRuleUrl();
            }
        }.go();
        Collections.sort(rules);
        String key = Formats.join(rules, ";");

        KnowledgeBase kb;
        synchronized (BASE_CACHE) {
            //nowy sposób, bazujący na cache knowledge package a nie knowledge base
            Collection<KnowledgePackage> knowledgePackages = PACK_CACHE.get(key);

            kb = BASE_CACHE.get(key);
            Long validTo = VALIDITY_CACHE.get(key);
            if (validTo != null && validTo < System.currentTimeMillis()) {
                log.warning("clearing cache for " + key);
                kb = null;
            }

            validTo = PACK_VALIDITY_CACHE.get(key);
            if (validTo != null && validTo < System.currentTimeMillis()) {
                log.warning("clearing packages cache for " + key);
                kb = null;
                knowledgePackages = null;
            }

            if (knowledgePackages == null) {
                knowledgePackages = getPackages(droolsResources);
                PACK_CACHE.put(key, knowledgePackages);
                PACK_VALIDITY_CACHE.put(key, System.currentTimeMillis() + PACK_VALIDITY_TIME);
                log.warning("registering drools packages for " + key);
            }
            else {
                log.fine("found cached drools packages for " + key);
            }
            if (kb == null) {
                kb = getKbase(knowledgePackages);
                BASE_CACHE.put(key, kb);
                VALIDITY_CACHE.put(key, System.currentTimeMillis() + VALIDITY_TIME);
                log.warning("registering KnowledgeBase for " + key);
            }
            else {
                log.fine("found cached KnowledgeBase for " + key);
            }
        }
        return kb;
    }

	private static KnowledgeBase getCachedKnowledgeBase(String... rulesUrls) {
        return getCachedKnowledgeBase(transformRuleUrls(rulesUrls));
	}

	public static synchronized StatelessKnowledgeSession getCachedStatelessSession(String... rulesUrls) {
		KnowledgeBase kb = getCachedKnowledgeBase(rulesUrls);
		long t = System.currentTimeMillis();
		try {
			return initStatelessSession(kb);
		}
		finally {
			log.fine("initStatelessSession took: " + (System.currentTimeMillis() - t));
		}
	}

	public synchronized static StatefulKnowledgeSession getSession(String... rulesUrls) {
		KnowledgeBase kbase = getKbase(rulesUrls);
		return initSession(kbase);
	}

	private static StatefulKnowledgeSession initSession(KnowledgeBase kbase) {
		StatefulKnowledgeSession session = null;
		if (kbase != null) {
			synchronized (kbase) {
				session = kbase.newStatefulKnowledgeSession();
			}
		}
		return session;
	}

	private static StatelessKnowledgeSession initStatelessSession(KnowledgeBase kbase) {
		StatelessKnowledgeSession session = null;
		if (kbase != null) {
			synchronized (kbase) {
				session = kbase.newStatelessKnowledgeSession();
				if (log.isLoggable(Level.FINEST))
					KnowledgeRuntimeLoggerFactory.newConsoleLogger(session);
			}
		}
		return session;
	}

	private static KnowledgeBase getKbase(String... rulesUrls) {
		Collection<KnowledgePackage> knowledgePackages = getPackages(rulesUrls);
		return getKbase(knowledgePackages);
	}

	private static KnowledgeBase getKbase(Collection<KnowledgePackage> knowledgePackages) {
		KnowledgeBase kbase = null;
		if (!knowledgePackages.isEmpty()) {
			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			kbase.addKnowledgePackages(knowledgePackages);
		}
		return kbase;
	}

    private static Collection<KnowledgePackage> getPackages(DroolsResource... droolsResources) {
        MVELDialectConfiguration conf = (MVELDialectConfiguration) new PackageBuilderConfiguration().getDialectConfiguration("mvel");
        conf.setStrict(false);
        conf.getPackageBuilderConfiguration().setDefaultDialect("mvel");

        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(conf.getPackageBuilderConfiguration());
        for (DroolsResource res : droolsResources) {
            builder.add(res.getRuleStream() != null ? ResourceFactory.newInputStreamResource(res.getRuleStream())
                    : ResourceFactory.newUrlResource(res.getRuleUrl()), ResourceType.DRL);
        }
        if (builder.hasErrors()) throw new RuntimeException(builder.getErrors().toString());
        Collection<KnowledgePackage> knowledgePackages = builder.getKnowledgePackages();
        return knowledgePackages;
    }

	private static Collection<KnowledgePackage> getPackages(String... rulesUrls) {
        return getPackages(transformRuleUrls(rulesUrls));
	}
}
