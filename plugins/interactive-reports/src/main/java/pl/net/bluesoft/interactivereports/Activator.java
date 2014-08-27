package pl.net.bluesoft.interactivereports;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.interactivereports.bundleext.InteractiveReportsBundleExtensionHandler;
import pl.net.bluesoft.interactivereports.templates.HtmlInteractiveReportTemplateLoader;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper;

/**
 * User: POlszewski
 * Date: 2014-06-24
 */
public class Activator implements BundleActivator {
	@Autowired
	private ProcessToolRegistry registry;

	@Override
	public void start(BundleContext context) throws Exception {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		registry.getBundleRegistry().registerBundleExtensionHandler(InteractiveReportsBundleExtensionHandler.INSTANCE);

		IBundleResourceProvider bundleResourceProvider = new OSGiBundleHelper(context.getBundle());
		String definitions = bundleResourceProvider.getBundleResourceString("/interactive_reports/definitions.html");
		HtmlInteractiveReportTemplateLoader.setDefinitions(definitions);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		registry.getBundleRegistry().unregisterBundleExtensionHandler(InteractiveReportsBundleExtensionHandler.INSTANCE);
	}
}
