package org.aperteworkflow.ext.activiti;

import com.thoughtworks.xstream.XStream;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.db.IbatisVariableTypeHandler;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.variable.VariableType;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.aperteworkflow.ext.activiti.wrappers.DataSourceWrapper;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.io.IOUtils;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
//TODO introduce abstract common ContextFactory for common methods
public class ActivitiContextFactoryImpl implements ProcessToolContextFactory {
    private static Logger logger = Logger.getLogger(ActivitiContextFactoryImpl.class.getName());
    private ProcessToolRegistry registry;

    public ActivitiContextFactoryImpl(ProcessToolRegistry registry) {
        this.registry = registry;

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
        Session session = registry.getSessionFactory().openSession();
        try {
            CustomStandaloneProcessEngineConfiguration processEngineConfiguration = getProcessEngineConfiguration(session);
            ProcessEngine pi = getProcessEngine(processEngineConfiguration);
            try {
                org.hibernate.Transaction tx = session.beginTransaction();
                T res;
                try {
                    ActivitiContextImpl ctx = new ActivitiContextImpl(session, this, pi, processEngineConfiguration);
                    res = callback.processWithContext(ctx);
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
                return res;
            } finally {
                pi.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public <T> T withProcessToolContextJta(ReturningProcessToolContextCallback<T> callback) {
        try {
            UserTransaction ut;
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
            T res;
            try {
                //init activiti
                CustomStandaloneProcessEngineConfiguration processEngineConfiguration = getProcessEngineConfiguration(session);
                ProcessEngine processEngine = getProcessEngine(processEngineConfiguration);
                try {
                    try {
                        ActivitiContextImpl ctx = new ActivitiContextImpl(session, this, processEngine, processEngineConfiguration);
                        res = callback.processWithContext(ctx);
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
                    processEngine.close();
                }
            } finally {
                session.flush();
            }
            ut.commit();
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * We need to alter activiti mappings a little,
     * since Activiti does not provide any other way to add custom mappings
     * (was the abandonment of Hibernate a right decision?)
     */
    public class CustomStandaloneProcessEngineConfiguration extends StandaloneProcessEngineConfiguration {

        @Override
        protected void initSqlSessionFactory() {
            if (sqlSessionFactory == null) {
                InputStream inputStream = null;
                try {
                    inputStream = ReflectUtil.getResourceAsStream("org/aperteworkflow/ext/activiti/mybatis/mappings-enhanced.xml");

                    // update the jdbc parameters to the configured ones...
                    Environment environment = new Environment("default", transactionFactory, dataSource);
                    Reader reader = new InputStreamReader(inputStream);
                    XMLConfigBuilder parser = new XMLConfigBuilder(reader);
                    Configuration configuration = parser.getConfiguration();
                    configuration.setEnvironment(environment);
                    configuration.getTypeHandlerRegistry().register(VariableType.class, JdbcType.VARCHAR,
                            new IbatisVariableTypeHandler());
                    configuration = parser.parse();

                    sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

                } catch (Exception e) {
                    throw new ActivitiException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
                } finally {
                    IoUtil.closeSilently(inputStream);
                }
            }
        }
    }

    public CustomStandaloneProcessEngineConfiguration getProcessEngineConfiguration(Session sess) {
        CustomStandaloneProcessEngineConfiguration customStandaloneProcessEngineConfiguration = new CustomStandaloneProcessEngineConfiguration();
                customStandaloneProcessEngineConfiguration
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .setDataSource(getDataSourceWrapper(sess))
                .setHistory(ProcessEngineConfiguration.HISTORY_FULL)
                .setTransactionsExternallyManaged(true);
        return customStandaloneProcessEngineConfiguration;
    }

    public ProcessEngine getProcessEngine(CustomStandaloneProcessEngineConfiguration customStandaloneProcessEngineConfiguration) {
        return customStandaloneProcessEngineConfiguration.buildProcessEngine();
    }

    private DataSource getDataSourceWrapper(Session sess) {
        return new DataSourceWrapper(sess);
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
                                                InputStream logoStream) {
        withProcessToolContext(new ProcessToolContextCallback() {
            @Override
            public void withContext(ProcessToolContext processToolContext) {

                ProcessToolContext.Util.setThreadProcessToolContext(processToolContext);
                try {
                    boolean skipBpm = false;
                    InputStream is = bpmStream;
                    ProcessToolBpmSession session = processToolContext.getProcessToolSessionFactory().createSession(
                            new UserData("admin", "admin@aperteworkflow.org", "Admin"), Arrays.asList("ADMIN"));
                    byte[] oldDefinition = session.getProcessLatestDefinition(cfg.getBpmDefinitionKey(), cfg.getProcessName());
                    if (oldDefinition != null) {
                        byte[] newDefinition = IOUtils.slurp(is);
                        is = new ByteArrayInputStream(newDefinition);
                        if (Arrays.equals(newDefinition, oldDefinition)) {
                            logger.log(Level.WARNING, "bpm definition for " + cfg.getProcessName() +
                                    " is the same as in BPM, therefore not updating BPM process definition");
                            skipBpm = true;
                        }
                    }

                    if (!skipBpm) {
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
                                                InputStream imageStream,
                                                InputStream logoStream) {


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
            byte[] logoBytes;
            try {
                logoBytes = IOUtils.slurp(logoStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    @Override
    public void updateSessionFactory(org.hibernate.SessionFactory sf) {
        //nothing
    }
}
