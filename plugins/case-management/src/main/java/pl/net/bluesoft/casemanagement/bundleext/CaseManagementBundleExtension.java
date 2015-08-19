package pl.net.bluesoft.casemanagement.bundleext;

import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.casemanagement.deployment.CaseDeployer;
import pl.net.bluesoft.casemanagement.model.CaseDefinition;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.BundleExtensionHandler;
import pl.net.bluesoft.rnd.processtool.plugins.BundleExtensionHandlerParams;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2014-07-04
 */
public class CaseManagementBundleExtension implements BundleExtensionHandler {
	public static final CaseManagementBundleExtension INSTANCE = new CaseManagementBundleExtension();

	private static final Logger LOGGER = Logger.getLogger(CaseManagementBundleExtension.class.getName());
	private static final String HEADER_NAME = "Case-Deployment";

	@Autowired
	private ProcessToolRegistry registry;

	@Override
	public void handleBundleExtensions(BundleExtensionHandlerParams params) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		if (params.hasBundleHeader(HEADER_NAME)) {
			String[] caseFiles = params.getBundleHeaderValues(HEADER_NAME);
			final CaseDeployer deployer = new CaseDeployer();

			for (String caseFile : caseFiles) {
				if (params.getEventType() == Bundle.ACTIVE) {

					try {
						if (!caseFile.startsWith("/")) {
							caseFile = '/' + caseFile;
						}

						InputStream stream = params.getBundleResourceProvider().getBundleResourceStream(caseFile);
						final CaseDefinition caseDefinition = deployer.unmarshallCaseDefinition(stream);

						registry.withProcessToolContext(new ProcessToolContextCallback() {
							@Override
							public void withContext(ProcessToolContext ctx) {
								deployer.deployOrUpdateCaseDefinition(caseDefinition, ctx.getHibernateSession());
							}
						});
					}
					catch (Exception e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		}
	}
}
