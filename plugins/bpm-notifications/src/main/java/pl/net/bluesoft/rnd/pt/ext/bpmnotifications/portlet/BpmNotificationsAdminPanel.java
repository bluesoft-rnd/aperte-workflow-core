package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components.*;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:19
 */
public class BpmNotificationsAdminPanel extends CustomComponent implements TabSheet.SelectedTabChangeListener {
	private TabSheet tabSheet;

	private I18NSource i18NSource;
	private ProcessToolRegistry registry;

	public BpmNotificationsAdminPanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		this.i18NSource = i18NSource;
		this.registry = registry;

		setCompositionRoot(buildLayout());
	}

	private Component buildLayout() {
		tabSheet = new TabSheet();
		tabSheet.setWidth("100%");
		tabSheet.setImmediate(true);
		tabSheet.addListener(this);

		tabSheet.addTab(new MailPropertiesPanel(i18NSource, registry), getMessage("Ustawienia konta"));
		tabSheet.addTab(new TemplatePanel(i18NSource, registry), getMessage("Szablony"));
		tabSheet.addTab(new NotificationPanel(i18NSource, registry), getMessage("Powiadomienia"));
		tabSheet.addTab(new NotificationHistoryPanel(i18NSource, registry), getMessage("Wysłane powiadomienia"));
		tabSheet.addTab(new OthersPanel(i18NSource, registry), getMessage("Pozostałe"));

		return tabSheet;
	}

	private String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	@Override
	public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
		if (tabSheet.getSelectedTab() instanceof DataLoadable) {
			((DataLoadable)tabSheet.getSelectedTab()).loadData();
		}
	}
}
