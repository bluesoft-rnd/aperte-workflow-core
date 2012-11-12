package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import org.aperteworkflow.util.vaadin.ui.Dialog;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-10-13
 * Time: 22:53
 */
public class NotificationEntryDetailsDialog extends Dialog {
	private I18NSource i18NSource;

	private Label subject;
	private Label body;

	public NotificationEntryDetailsDialog(I18NSource i18NSource) {
		super(i18NSource.getMessage("Szczegóły powiadomienia"));
		this.i18NSource = i18NSource;
		buildDialogLayout();
	}

	private void buildDialogLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.addComponent(subject = new Label());
		formLayout.addComponent(body = new Label());

		subject.setCaption(getMessage("Tytuł"));
		body.setCaption(getMessage("Treść"));

		addDialogContent(formLayout);

		addDialogAction(getMessage("Zamknij"), null);
	}

	private String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	public void setEntry(NotificationHistoryEntry item) {
		if (item.isAsHtml()) {
			subject.setContentMode(Label.CONTENT_XHTML);
			body.setContentMode(Label.CONTENT_XHTML);
		}
		subject.setValue(item.getSubject());
		body.setValue(item.getBody());
	}
}
