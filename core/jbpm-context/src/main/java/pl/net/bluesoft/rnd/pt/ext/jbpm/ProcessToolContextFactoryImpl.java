package pl.net.bluesoft.rnd.pt.ext.jbpm;

import com.thoughtworks.xstream.XStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.Strings;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolContextFactoryImpl implements ProcessToolContextFactory, ProcessToolBpmConstants {

    private static Logger logger = Logger.getLogger(ProcessToolContextFactoryImpl.class.getName());
    private Configuration configuration;
    private ProcessToolRegistry registry;

    public ProcessToolContextFactoryImpl(ProcessToolRegistry registry) {
        this.registry = registry;
        initJbpmConfiguration();
    }

    @Override
    public <T> T withExistingOrNewContext(ReturningProcessToolContextCallback<T> callback) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        return ctx != null && ctx.isActive() ? callback.processWithContext(ctx) : withProcessToolContext(callback);
    }

    @Override
	public <T> T withProcessToolContext(ReturningProcessToolContextCallback<T> callback) {
        if (registry.isJta()) {
            return withProcessToolContextJta(callback);
        } else {
            return withProcessToolContextNonJta(callback);
        }
    }

	public <T> T withProcessToolContextNonJta(ReturningProcessToolContextCallback<T> callback) {
        T result = null;

		Session session = registry.getSessionFactory().openSession();
		try {
			ProcessEngine pi = getProcessEngine();
			try {
				Transaction tx = session.beginTransaction();
				try {
					ProcessToolContextImpl ctx = new ProcessToolContextImpl(session, this, pi);
					result = callback.processWithContext(ctx);
				} catch (RuntimeException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					try {
						tx.rollback();
					} catch (Exception e1) {
						logger.log(Level.WARNING, e1.getMessage(), e1);
					}
					throw e;
				}
				tx.commit();
			}
			finally {
				pi.close();
			}
		} finally {
			session.close();
		}
        return result;
    }

    public <T> T withProcessToolContextJta(ReturningProcessToolContextCallback<T> callback) {
        T result = null;

        try {
            UserTransaction ut=null;
            try {
                ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            } catch (Exception e) {
                //it should work on jboss regardless. But it does not..
                logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
                ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
            }

            logger.fine("ut.getStatus() = " + ut.getStatus());
            if (ut.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                ut.rollback();
            }
            if (ut.getStatus() != Status.STATUS_ACTIVE)
                ut.begin();
            Session session = registry.getSessionFactory().getCurrentSession();
            try {
                ProcessEngine pi = getProcessEngine();
                try {
                    try {
                        ProcessToolContextImpl ctx = new ProcessToolContextImpl(session, this, pi);
                        result = callback.processWithContext(ctx);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        try {
                            ut.rollback();
                        } catch (Exception e1) {
                            logger.log(Level.WARNING, e1.getMessage(), e1);
                        }
                        throw e;
                    }
                } finally {
                    pi.close();
                }
            } finally {
                session.flush();
            }
            ut.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;


    }

    private ProcessEngine getProcessEngine() {
        Thread t = Thread.currentThread();
        ClassLoader previousLoader = t.getContextClassLoader();
        try {
            ClassLoader newClassLoader = getClass().getClassLoader();
            t.setContextClassLoader(newClassLoader);
            return configuration.buildProcessEngine();
        } finally {
            t.setContextClassLoader(previousLoader);
        }
    }

    @Override
    public ProcessToolRegistry getRegistry() {
        return registry;
    }


    @Override
    public void deployOrUpdateProcessDefinition(final InputStream bpmStream,
                                                final ProcessDefinitionConfig cfg,
                                                final ProcessQueueConfig[] queues,
                                                final InputStream imageStream,
                                                final InputStream logoStream) {
        withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext processToolContext) {

                ProcessToolContext.Util.setThreadProcessToolContext(processToolContext);
                try {
                    boolean skipJbpm = false;
                    InputStream is = bpmStream;
                    ProcessToolBpmSession session = processToolContext.getProcessToolSessionFactory().createSession(
                            new UserData("admin", "admin@aperteworkflow.org", "Admin"), Arrays.asList("ADMIN"));
                    if (cfg.getPermissions() != null) {
                        for (ProcessDefinitionPermission p : cfg.getPermissions()) {
                            if (!Strings.hasText(p.getPrivilegeName())) {
                                p.setPrivilegeName(PRIVILEGE_INCLUDE);
                            }
                            if (!Strings.hasText(p.getRoleName())) {
                                p.setRoleName(PATTERN_MATCH_ALL);
                            }
                        }
                    }
                    byte[] oldDefinition = session.getProcessLatestDefinition(cfg.getBpmDefinitionKey(), cfg.getProcessName());
                    if (oldDefinition != null) {
                        byte[] newDefinition = loadBytesFromStream(is);
                        is = new ByteArrayInputStream(newDefinition);
                        if (Arrays.equals(newDefinition, oldDefinition)) {
                            logger.log(Level.WARNING, "bpm definition for " + cfg.getProcessName() +
                                    " is the same as in BPM, therefore not updating BPM process definition");
                            skipJbpm = true;
                        }
                    }

                    if (!skipJbpm) {
                        String deploymentId = session.deployProcessDefinition(cfg.getProcessName(), is, imageStream);
                        logger.log(Level.INFO, "deployed new BPM Engine definition with id: " + deploymentId);
                    }

                    ProcessDefinitionDAO processDefinitionDAO = processToolContext.getProcessDefinitionDAO();
                    processDefinitionDAO.updateOrCreateProcessDefinitionConfig(cfg);
                    logger.log(Level.INFO, "created  definition with id: " + cfg.getId());
                    if (queues != null && queues.length > 0) {
                        processDefinitionDAO.updateOrCreateQueueConfigs(Arrays.asList(queues));
                        logger.log(Level.INFO, "created/updated " + queues.length + " queues");
                    }
                } finally {
                    ProcessToolContext.Util.removeThreadProcessToolContext();
                }
            }
        });
    }

    @Override
    public void deployOrUpdateProcessDefinition(InputStream jpdlStream,
                                                InputStream processToolConfigStream,
                                                InputStream queueConfigStream,
                                                final InputStream imageStream,
                                                final InputStream logoStream) {
        if (jpdlStream == null || processToolConfigStream == null || queueConfigStream == null) {
            throw new IllegalArgumentException("at least one of the streams is null");
        }
        XStream xstream = new XStream();
        xstream.aliasPackage("config", ProcessDefinitionConfig.class.getPackage().getName());
        xstream.useAttributeFor(String.class);
        xstream.useAttributeFor(Boolean.class);
        xstream.useAttributeFor(Integer.class);

        ProcessDefinitionConfig config = (ProcessDefinitionConfig) xstream.fromXML(processToolConfigStream);

        if (logoStream != null) {
            byte[] logoBytes = loadBytesFromStream(logoStream);
            if (logoBytes.length > 0) {
                config.setProcessLogo(logoBytes);
            }
        }
        Collection<ProcessQueueConfig> qConfigs = (Collection<ProcessQueueConfig>) xstream.fromXML(queueConfigStream);
        deployOrUpdateProcessDefinition(jpdlStream,
                config,
                qConfigs.toArray(new ProcessQueueConfig[qConfigs.size()]),
                imageStream,
                logoStream);
    }

//    }

    private byte[] loadBytesFromStream(InputStream stream) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c;
        try {
            while ((c = stream.read()) >= 0) {
                bos.write(c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    public void updateSessionFactory(SessionFactory sf) {
        if (configuration != null) {
            configuration.setHibernateSessionFactory(sf);
        }
    }

    public void initJbpmConfiguration() {
        Thread t = Thread.currentThread();
        ClassLoader previousLoader = t.getContextClassLoader();
        try {
            ClassLoader newClassLoader = getClass().getClassLoader();
            t.setContextClassLoader(newClassLoader);
            configuration = new Configuration();
            configuration.setHibernateSessionFactory(registry.getSessionFactory());
        } finally {
            t.setContextClassLoader(previousLoader);
        }
    }

}
