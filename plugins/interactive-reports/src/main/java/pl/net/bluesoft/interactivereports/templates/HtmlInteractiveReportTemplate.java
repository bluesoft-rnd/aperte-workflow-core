package pl.net.bluesoft.interactivereports.templates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.interactivereports.util.Dates;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;
import pl.net.bluesoft.rnd.processtool.dict.IDictionaryFacade;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;

import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * User: POlszewski
 * Date: 2014-06-25
 */
public abstract class HtmlInteractiveReportTemplate extends DefaultInteractiveReportTemplate {
	private final HtmlInteractiveReportTemplateLoader templateLoader;

	@Autowired
	private IDictionaryFacade dictionaryFacade;

	@Autowired
	private ISettingsProvider settingsProvider;

	protected HtmlInteractiveReportTemplate(IBundleResourceProvider bundleResourceProvider, String name, String templatePath) {
		super(name);
		this.templateLoader = new HtmlInteractiveReportTemplateLoader(bundleResourceProvider, templatePath);
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public String renderReportParams(RenderParams params) {
		return templateLoader.processParamsTemplate(getTemplateParams(params));
	}

	@Override
	public String renderReport(RenderParams params) {
		beforeRenderReport(params);
		return templateLoader.processBodyTemplate(getTemplateParams(params));
	}

	protected void beforeRenderReport(RenderParams params) {}

	private Map<String, Object> getTemplateParams(RenderParams params) {
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("messageSource", params.getMessageSource());
		result.put("user", params.getUser());
		result.put("dictionariesFacade", dictionaryFacade);
		result.put("dictionaryDao", getThreadProcessToolContext().getProcessDictionaryDAO());
		result.put("settingsProvider", settingsProvider);
		result.put("dates", new Dates());

		if (params.getReportParams() != null) {
			for (Map.Entry<String, Object> entry : params.getReportParams().entrySet()) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
}
