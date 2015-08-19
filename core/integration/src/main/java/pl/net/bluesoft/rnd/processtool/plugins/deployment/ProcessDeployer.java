package pl.net.bluesoft.rnd.processtool.plugins.deployment;

import com.thoughtworks.xstream.XStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.Strings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Process Definition deployer
 * 
 * @author mpawlak@bluesoft.net.pl
 * 
 */
public class ProcessDeployer 
{
	private static final Logger logger = Logger.getLogger(ProcessDeployer.class.getName());
	
	private ProcessToolContext processToolContext;

    @Autowired
    private ProcessToolRegistry processToolRegistry;

	public ProcessDeployer(ProcessToolContext processToolContext)
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		this.processToolContext = processToolContext;
	}
	
	/**
	 * Unmarshall {@link ProcessDefinitionConfig} instance object from input stream
	 * @param processToolConfigStream
	 * @return
	 */
	public ProcessDefinitionConfig unmarshallProcessDefinition(InputStream processToolConfigStream)
	{
		XStream xstream = new XStream();
		xstream.aliasPackage("config", ProcessDefinitionConfig.class
				.getPackage().getName());
		xstream.useAttributeFor(String.class);
		xstream.useAttributeFor(Boolean.class);
		xstream.useAttributeFor(Integer.class);

		return (ProcessDefinitionConfig) xstream.fromXML(processToolConfigStream);
	}

	private void deployOrUpdateProcessDefinition(InputStream jpdlStream, ProcessDefinitionConfig cfg,
												ProcessQueueConfig[] queues, InputStream imageStream) {
		ProcessToolBpmSession session = createAdminSession();
		ProcessDefinitionDAO processDefinitionDAO = processToolContext.getProcessDefinitionDAO();

		adjustPriviledges(cfg);

		cfg.setBpmDefinitionVersion(processDefinitionDAO.getNextProcessVersion(cfg.getBpmDefinitionKey()));

		byte[] newDefinition = loadBytesFromStream(jpdlStream);

		if (session.differsFromTheLatest(cfg.getBpmDefinitionKey(), newDefinition) ||
			processDefinitionDAO.differsFromTheLatest(cfg)) {


			String deploymentId = session.deployProcessDefinition(cfg.getBpmProcessId(),
					new ByteArrayInputStream(newDefinition), imageStream);

            if (deploymentId != null) {
                logger.log(Level.INFO, "deployed new BPM Engine definition with id: " + deploymentId);

                cfg.setDeploymentId(deploymentId);

                processDefinitionDAO.updateOrCreateProcessDefinitionConfig(cfg);

                logger.log(Level.INFO, "created definition with id: " + cfg.getId() + ", processId: " + cfg.getBpmProcessId());
            }
            else{
                logger.log(Level.INFO, "Failed to build knowledge base, error in definition. Process has not been deployed: " + cfg.getBpmDefinitionKey());
            }
		}
		else {
			logger.warning("New process " + cfg.getBpmDefinitionKey() +
					" definition is the same as existing one. Therefore skipping DB update");
		}

		if (queues != null && queues.length > 0) {
			processDefinitionDAO.updateOrCreateQueueConfigs(Arrays.asList(queues));
			logger.log(Level.INFO, "created/updated " + queues.length + " queues");
		}
	}

	private void adjustPriviledges(ProcessDefinitionConfig cfg) {
		if (cfg.getPermissions() != null) {
			for (ProcessDefinitionPermission p : cfg.getPermissions()) {
				if (!Strings.hasText(p.getPrivilegeName())) {
					p.setPrivilegeName(ProcessToolBpmConstants.PRIVILEGE_INCLUDE);
				}
				if (!Strings.hasText(p.getRoleName())) {
					p.setRoleName(ProcessToolBpmConstants.PATTERN_MATCH_ALL);
				}
			}
		}
	}

	private ProcessToolBpmSession createAdminSession() {
		return processToolRegistry.getProcessToolSessionFactory().createSession("admin", Collections.singletonList("ADMIN"));
	}

	public String deployOrUpdateProcessDefinition(
			InputStream jpdlStream,
			InputStream processToolConfigStream, 
			InputStream queueConfigStream,
			InputStream imageStream, 
			InputStream logoStream) 
	{
		checkRequiredFiles(jpdlStream, processToolConfigStream, queueConfigStream);
		XStream xstream = new XStream();
		xstream.aliasPackage("config", ProcessDefinitionConfig.class
				.getPackage().getName());
		xstream.useAttributeFor(String.class);
		xstream.useAttributeFor(Boolean.class);
		xstream.useAttributeFor(Integer.class);

		ProcessDefinitionConfig config = (ProcessDefinitionConfig) xstream
				.fromXML(processToolConfigStream);

//		if (logoStream != null) {
//			byte[] logoBytes = loadBytesFromStream(logoStream);
//			if (logoBytes.length > 0) {
//				config.setProcessLogo(logoBytes);
//			}
//		}
		Collection<ProcessQueueConfig> qConfigs = (Collection<ProcessQueueConfig>) xstream.fromXML(queueConfigStream);
		deployOrUpdateProcessDefinition(jpdlStream, config,
				qConfigs.toArray(new ProcessQueueConfig[qConfigs.size()]),
				imageStream);

		return config.getBpmDefinitionKey();

	}

	private void checkRequiredFiles(InputStream jpdlStream, InputStream processToolConfigStream, InputStream queueConfigStream) {
		if (jpdlStream == null) {
			throw new IllegalArgumentException("processdefinition." + processToolRegistry.getProcessToolSessionFactory().getBpmDefinitionLanguage() + " file missing");
		}
		if (processToolConfigStream == null) {
			throw new IllegalArgumentException("processtool-config.xml file missing");
		}
		if (queueConfigStream == null) {
			throw new IllegalArgumentException("queues-config.xml file missing");
		}
	}

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
}
