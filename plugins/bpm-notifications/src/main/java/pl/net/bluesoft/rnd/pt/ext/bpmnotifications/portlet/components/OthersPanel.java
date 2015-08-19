package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import static com.vaadin.ui.Window.Notification.POSITION_CENTERED;
import static com.vaadin.ui.Window.Notification.TYPE_HUMANIZED_MESSAGE;

import pl.net.bluesoft.rnd.processtool.model.UserDataBean;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.IBpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.ProcessedNotificationData;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:23
 *
 * @author mpawlak@bluesoft.net.pl
 */
public class OthersPanel extends VerticalLayout implements Button.ClickListener {
	private Button refreshCachesBtn;

	private TextField senderTextField;
	private TextField recipientTextField;
	private Button sendTestEmailButton;

	private I18NSource i18NSource;
	private ProcessToolRegistry registry;

	public OthersPanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		this.i18NSource = i18NSource;
		this.registry = registry;

		buildLayout();
	}

	private void buildLayout() {
		setSpacing(true);

		refreshCachesBtn = new Button(getMessage("bpmnot.refresh.config.cache"));
		refreshCachesBtn.addListener((Button.ClickListener)this);

		addComponent(refreshCachesBtn);

		HorizontalLayout sendTestEmailLayout = new HorizontalLayout();
		sendTestEmailLayout.setSpacing(true);

		senderTextField = new TextField();
		senderTextField.setWidth(150, UNITS_PIXELS);
		senderTextField.setInputPrompt(i18NSource.getMessage("bpmnot.send.test.mail.sender"));

		recipientTextField = new TextField();
		recipientTextField.setInputPrompt(i18NSource.getMessage("bpmnot.send.test.mail.recipient"));
		recipientTextField.setWidth(150, UNITS_PIXELS);

		sendTestEmailButton = new Button(i18NSource.getMessage("bpmnot.send.test.mail.send.button"));
		sendTestEmailButton.addListener((Button.ClickListener)this);

		sendTestEmailLayout.addComponent(senderTextField);
		sendTestEmailLayout.addComponent(recipientTextField);
		sendTestEmailLayout.addComponent(sendTestEmailButton);

		addComponent(sendTestEmailLayout);
	}

	@Override
	public void buttonClick(Button.ClickEvent event) {
		if (event.getSource() == refreshCachesBtn) {
			getService().invalidateCache();
		}
		else if(event.getButton().equals(sendTestEmailButton))
		{
			try
			{
				String sender = (String)senderTextField.getValue();
				String recipient = (String)recipientTextField.getValue();

				if(sender == null || sender.isEmpty())
				{
					informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.sender.empty"));
					return;
				}

				if(recipient == null || recipient.isEmpty())
				{
					informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.recipient.empty"));
					return;
				}

                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("aperte-delay-emails", false);

				UserDataBean recipientUser = new UserDataBean();
                recipientUser.setAttributes(attributes);
				recipientUser.setEmail(recipient);
				
				ProcessedNotificationData notificationData = new ProcessedNotificationData();
                notificationData
	            	.setBody("tekst <br><b>tekst html</b><br> tekst polski: żołądków")
	            	.setSubject("Test E-mail")
	            	.setSender(sender)
	            	.setProfileName("Default")
	            	.setRecipient(recipientUser);

				getService().addNotificationToSend(notificationData);
				informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.sent"));
			}
			catch (Exception e)
			{
				informationNotification("Problem: "+e.getMessage());
			}
		}

	}

	protected String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	private IBpmNotificationService getService() {
		return registry.getRegisteredService(IBpmNotificationService.class);
	}

	private void informationNotification(String message) {
		Window.Notification notification = new Window.Notification("<b>" + message + "</b>", TYPE_HUMANIZED_MESSAGE);
		notification.setPosition(POSITION_CENTERED);
		notification.setDelayMsec(5);
		this.getWindow().showNotification(notification);
	}
}
