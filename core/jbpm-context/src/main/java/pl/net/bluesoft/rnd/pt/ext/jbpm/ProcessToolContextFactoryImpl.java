package pl.net.bluesoft.rnd.pt.ext.jbpm;

import com.thoughtworks.xstream.XStream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jbpm.api.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolContextFactoryImpl implements ProcessToolContextFactory {

    private static Logger logger = Logger.getLogger(ProcessToolContextFactoryImpl.class.getName());
    private Configuration configuration;
    private ProcessToolRegistry registry;

    public ProcessToolContextFactoryImpl(ProcessToolRegistry registry) {
        this.registry = registry;
        initJbpmConfiguration();
    }

    @Override
	public void withProcessToolContext(ProcessToolContextCallback callback) {
        if (registry.isJta()) {
            withProcessToolContextJta(callback);
        } else {
            withProcessToolContextNonJta(callback);
        }
    }
	public void withProcessToolContextNonJta(ProcessToolContextCallback callback) {
		long t = System.currentTimeMillis();
		Session session = registry.getSessionFactory().openSession();
		try {
			ProcessEngine pi = getProcessEngine();
			try {
				Transaction tx = session.beginTransaction();
				try {
					ProcessToolContextImpl ctx = new ProcessToolContextImpl(session, this, pi);
					callback.withContext(ctx);
				} catch (RuntimeException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
					try {
						tx.rollback();
					}
					catch (Exception e1) {
						logger.log(Level.WARNING, e1.getMessage(), e);
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


    }

    public void withProcessToolContextJta(ProcessToolContextCallback callback) {
        try {
            UserTransaction ut=null;
            try {
                ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
            } catch (Exception e) {
                //it should work on jboss regardless. But it does not..
                logger.warning("java:comp/UserTransaction not found, looking for UserTransaction");
                ut = (UserTransaction) new InitialContext().lookup("UserTransaction");
            }
            
            
            ut.begin();
            Session session = registry.getSessionFactory().getCurrentSession();
            try {
                ProcessEngine pi = getProcessEngine();
                try {
                    try {
                        ProcessToolContextImpl ctx = new ProcessToolContextImpl(session, this, pi);
                        callback.withContext(ctx);
                    } catch (RuntimeException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                        try {
                            ut.rollback();
                        } catch (Exception e1) {
                            logger.log(Level.WARNING, e1.getMessage(), e);
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

    }

    private ProcessEngine getProcessEngine() {
        Thread t = Thread.currentThread();
        ClassLoader previousLoader = t.getContextClassLoader();
        try {
            ClassLoader newClassLoader = getClass().getClassLoader();
            System.out.println(newClassLoader);
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

                boolean skipJbpm = false;
                InputStream is = bpmStream;
                ProcessToolContextImpl impl = (ProcessToolContextImpl) processToolContext;
                RepositoryService service = impl.getProcessEngine().getRepositoryService();
                List<ProcessDefinition> latestList = service.createProcessDefinitionQuery()
                        .processDefinitionKey(cfg.getBpmDefinitionKey()).orderDesc("deployment.dbid").page(0, 1).list();
                if (!latestList.isEmpty()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int c;
                    try {
                        String oldDeploymentId = latestList.get(0).getDeploymentId();
                        InputStream oldStream = service.getResourceAsStream(oldDeploymentId, cfg.getProcessName() + ".jpdl.xml");
                        if (oldStream != null) {
                            while ((c = is.read()) >= 0) {
                                bos.write(c);
                            }
                            is = new ByteArrayInputStream(bos.toByteArray());
                            ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                            while ((c = oldStream.read()) >= 0) {
                                bos2.write(c);
                            }
                            if (bos.toByteArray().length == bos2.toByteArray().length) {
                                if (Arrays.equals(bos.toByteArray(), bos2.toByteArray())) {
                                    logger.log(Level.WARNING, "jbpm definition for " + cfg.getProcessName() +
                                            ".jpdl.xml is the same as in jBPM DB, therefore not updating jBPM process definition");
                                    skipJbpm = true;
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }

                if (!skipJbpm) {
                    NewDeployment deployment = service.createDeployment();
                    deployment.addResourceFromInputStream(cfg.getProcessName() + ".jpdl.xml", is);
                    if (imageStream != null)
                        deployment.addResourceFromInputStream(cfg.getProcessName() + ".png", imageStream);
                    String deploymentId = deployment.deploy();
                    logger.log(Level.INFO, "deployed definition with id: " + deploymentId);
                }

                ProcessDefinitionDAO processDefinitionDAO = processToolContext.getProcessDefinitionDAO();
                processDefinitionDAO.updateOrCreateProcessDefinitionConfig(cfg);
                logger.log(Level.INFO, "created  definition with id: " + cfg.getId());
                if (queues != null && queues.length > 0) {
                    processDefinitionDAO.updateOrCreateQueueConfigs(queues);
                    logger.log(Level.INFO, "created/updated " + queues.length + " queues");
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

//        Collection<ProcessQueueConfig> qConfigs = (Collection<ProcessQueueConfig>) xstream.fromXML(queueConfigStream);
//        deployOrUpdateProcessDefinition(jpdlStream, config, qConfigs.toArray(new ProcessQueueConfig[qConfigs.size()]));
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
            System.out.println(newClassLoader);
            t.setContextClassLoader(newClassLoader);
            configuration = new Configuration();
            configuration.setHibernateSessionFactory(registry.getSessionFactory());
        } finally {
            t.setContextClassLoader(previousLoader);
        }
    }

}
