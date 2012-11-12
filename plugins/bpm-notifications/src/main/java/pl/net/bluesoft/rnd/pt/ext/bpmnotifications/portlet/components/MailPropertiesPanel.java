package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationMailPropertiesDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:22
 */
public class MailPropertiesPanel extends ItemEditorLayout<BpmNotificationMailProperties> {
	private TextField profileName;
	private TextField smtpHost;
	private TextField smtpPort;
	private TextField smtpUser;
	private TextField smtpPassword;
	private CheckBox smtpAuth;
	private TextField smtpSocketFactoryPort;
	private TextField smtpSocketFactoryClass;
	private TextField sslSocketFactoryClass;
	private CheckBox disablePlainAuth;
	private TextField transportProtocol;
	private CheckBox startTls;
	private CheckBox debug;

	public MailPropertiesPanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		super(BpmNotificationMailProperties.class, i18NSource, registry);
		buildLayout();
	}

	@Override
	protected Component createItemDetailsLayout() {
		FormLayout formLayout = new FormLayout();

		formLayout.addComponent(profileName = textField("Profil", 400));
		formLayout.addComponent(smtpHost = textField("SMTP Host", 400));
		formLayout.addComponent(smtpPort = textField("SMTP Port", 400));
		formLayout.addComponent(smtpUser = textField("SMTP User", 400));
		formLayout.addComponent(smtpPassword = textField("SMTP Password", 400));
		formLayout.addComponent(smtpAuth = checkBox("SMTP Auth"));
		formLayout.addComponent(smtpSocketFactoryPort = textField("SMTP Socket Factory Port", 400));
		formLayout.addComponent(smtpSocketFactoryClass = textField("SMTP Socket Factory Class", -1));
		formLayout.addComponent(sslSocketFactoryClass = textField("SSL Socket Factory Class", -1));
		formLayout.addComponent(disablePlainAuth = checkBox("Disable Plain Auth"));
		formLayout.addComponent(transportProtocol = textField("Transport Protocol", 400));
		formLayout.addComponent(startTls = checkBox("Start TLS"));
		formLayout.addComponent(debug = checkBox("Debug"));

		return formLayout;
	}

	@Override
	protected void clearDetails() {
		profileName.setReadOnly(false);
		profileName.setValue(null);
		smtpHost.setValue(null);
		smtpPort.setValue(null);
		smtpUser.setValue(null);
		smtpPassword.setValue(null);
		smtpAuth.setValue(null);
		smtpSocketFactoryPort.setValue(null);
		smtpSocketFactoryClass.setValue(null);
		sslSocketFactoryClass.setValue(null);
		disablePlainAuth.setValue(null);
		transportProtocol.setValue(null);
		startTls.setValue(null);
		debug.setValue(null);
	}

	@Override
	protected void loadDetails(BpmNotificationMailProperties item) {
		profileName.setReadOnly(false);
		profileName.setValue(item.getProfileName());
		profileName.setReadOnly(item.getId() != null);
		smtpHost.setValue(item.getSmtpHost());
		smtpPort.setValue(item.getSmtpPort());
		smtpUser.setValue(item.getSmtpUser());
		smtpPassword.setValue(item.getSmtpPassword());
		smtpAuth.setValue(item.isSmtpAuth());
		smtpSocketFactoryPort.setValue(item.getSmtpSocketFactoryPort());
		smtpSocketFactoryClass.setValue(item.getSmtpSocketFactoryClass());
		sslSocketFactoryClass.setValue(item.getSslSocketFactoryClass());
		disablePlainAuth.setValue(item.isDisablePlainAuth());
		transportProtocol.setValue(item.getTransportProtocol());
		startTls.setValue(item.isStarttls());
		debug.setValue(item.isDebug());
	}

	@Override
	protected void saveDetails(BpmNotificationMailProperties item) {
		if (item.getId() == null) {
			item.setProfileName(getString(profileName));
		}
		item.setSmtpHost(getString(smtpHost));
		item.setSmtpPort(getString(smtpPort));
		item.setSmtpUser(getString(smtpUser));
		item.setSmtpPassword(getString(smtpPassword));
		item.setSmtpAuth(getBoolean(smtpAuth));
		item.setSmtpSocketFactoryPort(getString(smtpSocketFactoryPort));
		item.setSmtpSocketFactoryClass(getString(smtpSocketFactoryClass));
		item.setSslSocketFactoryClass(getString(sslSocketFactoryClass));
		item.setDisablePlainAuth(getBoolean(disablePlainAuth));
		item.setTransportProtocol(getString(transportProtocol));
		item.setStarttls(getBoolean(startTls));
		item.setDebug(getBoolean(debug));
	}

	@Override
	protected List<BpmNotificationMailProperties> getAllItems() {
		return new BpmNotificationMailPropertiesDAO().findAll();
	}

	@Override
	protected String getItemCaption(BpmNotificationMailProperties item) {
		return item.getProfileName();
	}

	@Override
	protected BpmNotificationMailProperties createItem() {
		return new BpmNotificationMailProperties();
	}

	@Override
	protected BpmNotificationMailProperties refreshItem(Long id) {
		return new BpmNotificationMailPropertiesDAO().loadById(id);
	}

	@Override
	protected void saveItem(BpmNotificationMailProperties item) {
		new BpmNotificationMailPropertiesDAO().saveOrUpdate(item);
	}
}
