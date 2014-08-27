package pl.net.bluesoft.interactivereports.bundleext;

import org.osgi.framework.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.interactivereports.service.InteractiveReportService;
import pl.net.bluesoft.interactivereports.templates.InteractiveReportTemplate;
import pl.net.bluesoft.rnd.processtool.plugins.BundleExtensionHandler;
import pl.net.bluesoft.rnd.processtool.plugins.BundleExtensionHandlerParams;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.util.AnnotationUtil;

/**
 * User: POlszewski
 * Date: 2014-06-26
 */
public class InteractiveReportsBundleExtensionHandler implements BundleExtensionHandler {
	public static final InteractiveReportsBundleExtensionHandler INSTANCE = new InteractiveReportsBundleExtensionHandler();

	private static final String HEADER_NAME = "Interactive-Reports";

	@Autowired
	private InteractiveReportService reportService;

	@Override
	public void handleBundleExtensions(BundleExtensionHandlerParams params) {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		if (params.hasBundleHeader(HEADER_NAME)) {
			String[] reportClassNames = params.getBundleHeaderValues(HEADER_NAME);

			for (String reportClassName : reportClassNames) {
				Class reportClass = params.loadClass(reportClassName);
				String reportKey = AnnotationUtil.getAliasName(reportClass);

				if (params.getEventType() == Bundle.ACTIVE) {
					InteractiveReportTemplate reportTemplate = createReportTemplate(params, reportClass);

					reportService.registerReportTemplate(reportKey, reportTemplate);
				}
				else {
					reportService.unregisterReportTemplate(reportKey);
				}
			}
		}
	}

	private InteractiveReportTemplate createReportTemplate(BundleExtensionHandlerParams params, Class reportClass) {
		try {
			return (InteractiveReportTemplate)reportClass.getConstructor(IBundleResourceProvider.class)
					.newInstance(params.getBundleResourceProvider());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
