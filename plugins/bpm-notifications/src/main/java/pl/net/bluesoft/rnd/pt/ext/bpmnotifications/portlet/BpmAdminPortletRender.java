package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet;

import org.aperteworkflow.ui.view.HtmlGenericPortletViewRenderer;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentDescription;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * User: POlszewski
 * Date: 2012-07-31
 * Time: 13:44
 */
public class BpmAdminPortletRender extends HtmlGenericPortletViewRenderer {
	public static BpmAdminPortletRender INSTANCE;

	public static void init(String html) {
		INSTANCE = new BpmAdminPortletRender(html);
	}

	protected BpmAdminPortletRender(String template) {
		super("bpm-notifications", "bpmnot.bpmnotifications", 100, template);
	}

	@Override
	protected Map<String, Object> getTemplateData() {
		Map<String, Object> result = new HashMap<String, Object>();

		I18NSource i18NSource = I18NSourceFactory.createI18NSource(Locale.getDefault());

		result.put("messageSource", i18NSource);
		result.put("templateInfo", getTemplateInfo(i18NSource));

		return result;
	}

	private String getTemplateInfo(I18NSource i18NSource) {
		StringBuilder sb = new StringBuilder();

		sb.append("<b>")
				.append(i18NSource.getMessage("bpmnot.parameters.below.can.be.placed.in.any.template"))
				.append("</b>");

		getParamDesc(sb, getService().getDefaultArgumentDescriptions(i18NSource));

		for (TemplateArgumentProvider argumentProvider : getService().getTemplateArgumentProviders()) {
			sb.append("<b>")
					.append(MessageFormat.format(i18NSource.getMessage("bpmnot.param.provider.x.provides"), argumentProvider.getName()))
					.append("</b>");

			getParamDesc(sb, argumentProvider.getArgumentDescriptions(i18NSource));
		}

		return sb.toString();
	}

	private void getParamDesc(StringBuilder sb, List<TemplateArgumentDescription> argDescriptions) {
		sb.append("<ul>");
		for (TemplateArgumentDescription defaultArg : argDescriptions) {

			sb.append("<li>")
					.append("${")
					.append(defaultArg.getName())
					.append("} - ")
					.append(defaultArg.getDescription())
					.append("</li>");
		}
		sb.append("</ul>");
	}

	private IBpmNotificationService getService() {
		return getRegistry().getRegisteredService(IBpmNotificationService.class);
	}
}
