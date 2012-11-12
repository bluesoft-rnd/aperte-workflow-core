package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import org.aperteworkflow.util.vaadin.LocalizedFormats;
import org.aperteworkflow.util.vaadin.ui.table.ReadOnlyTable;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry;
import static pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.NotificationHistoryEntry.*;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.SimpleDateFormat;


/**
 * User: POlszewski
 * Date: 2012-10-13
 * Time: 21:31
 */
public class NotificationHistoryTable extends ReadOnlyTable<NotificationHistoryEntry> {
	private SimpleDateFormat dateFormat;

	public NotificationHistoryTable(I18NSource i18NSource) {
		super(NotificationHistoryEntry.class, i18NSource);
		this.dateFormat = LocalizedFormats.getLongDateFormat(i18NSource);
		buildLayout("880px");
		setSelectable(true);
	}

	@Override
	protected String[] getPropertyNames() {
		return new String[] {
			_RECIPIENT, _SUBJECT, _ENQUEUE_DATE, _SEND_DATE,
		};
	}

	@Override
	protected String[] getPropertyColumnHeaders() {
		return new String[] {
			getMessage("Odbiorca"), getMessage("Tytuł"), getMessage("Przyjęto"), getMessage("Wysłano")
		};
	}

	@Override
	protected Integer[] getPropertyColumnWidths() {
		return new Integer[] { 200, 400, 110, null };
	}

	@Override
	protected String formatPropertyValue(NotificationHistoryEntry item, String property) {
		if (_ENQUEUE_DATE.equals(property)) {
			return item.getEnqueueDate() != null ? dateFormat.format(item.getEnqueueDate()) : "";
		}
		if (_SEND_DATE.equals(property)) {
			return item.getSendDate() != null ? dateFormat.format(item.getSendDate()) : "";
		}
		return super.formatPropertyValue(item, property);
	}

	@Override
	protected void handleItemSelection(NotificationHistoryEntry item) {
		NotificationEntryDetailsDialog dialog = new NotificationEntryDetailsDialog(i18NSource);
		dialog.setEntry(item);
		dialog.show(getApplication());
	}
}
