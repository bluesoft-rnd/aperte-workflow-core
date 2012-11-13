package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationTemplateDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentDescription;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:23
 */
public class TemplatePanel extends ItemEditorLayout<BpmNotificationTemplate> {
	private TextField templateName;
	private TextField templateSender;
	private TextField templateSubject;
	private TextArea templateBody;
	private Label argumentInfoLabel;

	public TemplatePanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		super(BpmNotificationTemplate.class, i18NSource, registry);
		buildLayout();
	}

	@Override
	protected Component createItemDetailsLayout() {
		FormLayout formLayout = new FormLayout();

		formLayout.addComponent(templateName = textField("Nazwa", 400));
		formLayout.addComponent(templateSender = textField("Nadawca", 400));
		formLayout.addComponent(templateSubject = textField("Tytuł", -1));
		formLayout.addComponent(templateBody = textArea("Treść", -1));

		return formLayout;
	}

	@Override
	protected Component createInfoLayout() {
		argumentInfoLabel = new Label("", Label.CONTENT_XHTML);
		return argumentInfoLabel;
	}

	@Override
	protected void clearDetails() {
		templateName.setReadOnly(false);
		templateName.setValue(null);
		templateSender.setValue(null);
		templateSubject.setValue(null);
		templateBody.setValue(null);
	}

	@Override
	protected void prepareData() {
		StringBuilder sb = new StringBuilder();

		sb.append("<b>")
			.append(getMessage("Poniższe parametry możesz umieścić w dowolnym szablonie:"))
			.append("</b>");

		getParamDesc(sb, getService().getDefaultArgumentDescriptions(getI18NSource()));

		for (TemplateArgumentProvider argumentProvider : getService().getTemplateArgumentProviders()) {
			sb.append("<b>")
				.append(MessageFormat.format(getMessage("Dodatkowo dostawca parametrów <i>{0}</i> udostępnia:"), argumentProvider.getName()))
				.append("</b>");

			getParamDesc(sb, argumentProvider.getArgumentDescriptions(getI18NSource()));
		}

		argumentInfoLabel.setValue(sb.toString());
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

	@Override
	protected void loadDetails(BpmNotificationTemplate item) {
		templateName.setReadOnly(false);
		templateName.setValue(item.getTemplateName());
		templateName.setReadOnly(item.getId() != null);
		templateSender.setValue(item.getSender());
		templateSubject.setValue(item.getSubjectTemplate());
		templateBody.setValue(item.getTemplateBody());
	}

	@Override
	protected void saveDetails(BpmNotificationTemplate item) {
		if (item.getId() == null) {
			item.setTemplateName(getString(templateName));
		}
		item.setSender(getString(templateSender));
		item.setSubjectTemplate(getString(templateSubject));
		item.setTemplateBody(getString(templateBody));
	}

	@Override
	protected List<BpmNotificationTemplate> getAllItems() {
		return new BpmNotificationTemplateDAO().findAll();
	}

	@Override
	protected String getItemCaption(BpmNotificationTemplate item) {
		return item.getTemplateName();
	}

	@Override
	protected BpmNotificationTemplate createItem() {
		return new BpmNotificationTemplate();
	}

	@Override
	protected BpmNotificationTemplate refreshItem(Long id) {
		return new BpmNotificationTemplateDAO().loadById(id);
	}

	@Override
	protected void saveItem(BpmNotificationTemplate item) {
		new BpmNotificationTemplateDAO().saveOrUpdate(item);
	}
}
