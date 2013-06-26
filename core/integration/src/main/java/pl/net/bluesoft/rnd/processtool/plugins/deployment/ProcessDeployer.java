package pl.net.bluesoft.rnd.processtool.plugins.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.util.lang.Strings;

import com.thoughtworks.xstream.XStream;

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

	public ProcessDeployer(ProcessToolContext processToolContext) {
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

		ProcessDefinitionConfig newProcessDefinition = (ProcessDefinitionConfig) xstream
				.fromXML(processToolConfigStream);
		
		return newProcessDefinition;
	}

	public void deployOrUpdateProcessDefinition(final InputStream jpdlStream,
			final ProcessDefinitionConfig cfg,
			final ProcessQueueConfig[] queues, final InputStream imageStream,
			InputStream logoStream) 
	{

		boolean skipJbpm = false;
		InputStream is = jpdlStream;

		ProcessToolBpmSession session = processToolContext
				.getProcessToolSessionFactory().createSession(
						new UserData("admin", "admin@aperteworkflow.org", "Admin"), Arrays.asList("ADMIN"));
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
			String deploymentId = session.deployProcessDefinition(
					cfg.getProcessName(),
					cfg.getBpmDefinitionKey(),
					is, imageStream);
			cfg.setDeploymentId(deploymentId);
			logger.log(Level.INFO, "deployed new BPM Engine definition with id: " + deploymentId);
		}

		ProcessDefinitionDAO processDefinitionDAO = processToolContext
				.getProcessDefinitionDAO();
		processDefinitionDAO.updateOrCreateProcessDefinitionConfig(cfg);
		logger.log(Level.INFO, "created  definition with id: " + cfg.getId());
		if (queues != null && queues.length > 0) {
			processDefinitionDAO.updateOrCreateQueueConfigs(Arrays
					.asList(queues));
			logger.log(Level.INFO, "created/updated " + queues.length
					+ " queues");
		}
	}

	public void deployOrUpdateProcessDefinition(
			InputStream jpdlStream,
			InputStream processToolConfigStream, 
			InputStream queueConfigStream,
			InputStream imageStream, 
			InputStream logoStream) 
	{
		if (jpdlStream == null || processToolConfigStream == null || queueConfigStream == null) {
			throw new IllegalArgumentException(
					"at least one of the streams is null");
		}
		XStream xstream = new XStream();
		xstream.aliasPackage("config", ProcessDefinitionConfig.class
				.getPackage().getName());
		xstream.useAttributeFor(String.class);
		xstream.useAttributeFor(Boolean.class);
		xstream.useAttributeFor(Integer.class);

		ProcessDefinitionConfig config = (ProcessDefinitionConfig) xstream
				.fromXML(processToolConfigStream);

		if (logoStream != null) {
			byte[] logoBytes = loadBytesFromStream(logoStream);
			if (logoBytes.length > 0) {
				config.setProcessLogo(logoBytes);
			}
		}
		Collection<ProcessQueueConfig> qConfigs = (Collection<ProcessQueueConfig>) xstream
				.fromXML(queueConfigStream);
		deployOrUpdateProcessDefinition(jpdlStream, config,
				qConfigs.toArray(new ProcessQueueConfig[qConfigs.size()]),
				imageStream, logoStream);
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
