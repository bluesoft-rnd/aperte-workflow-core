package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.List;

import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-10-13
 * Time: 21:29
 */
public class NotificationHistoryPanel extends VerticalLayout implements DataLoadable {
	private I18NSource i18NSource;
	private NotificationHistoryTable table;
	private ProcessToolRegistry registry;

	public NotificationHistoryPanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		this.i18NSource = i18NSource;
		this.registry = registry;
		buildLayout();
	}

	private void buildLayout() {
		setWidth("100%");
		setSpacing(true);

		table = new NotificationHistoryTable(i18NSource);
		addComponent(table);
	}

	@Override
	public void loadData() {
		List<NotificationHistoryEntry> notificationHistoryEntries = getService().getNotificationHistoryEntries();
		notificationHistoryEntries = from(notificationHistoryEntries).orderBy(new F<NotificationHistoryEntry, Comparable>() {
			@Override
			public Comparable invoke(NotificationHistoryEntry x) {
				return x.getEnqueueDate();
			}
		}).toList();
		table.setItems(notificationHistoryEntries);
	}

	protected BpmNotificationService getService() {
		return registry.getRegisteredService(BpmNotificationService.class);
	}
}
