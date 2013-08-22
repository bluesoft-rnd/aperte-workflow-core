package pl.net.bluesoft.rnd.processtool.portlets.report;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import org.aperteworkflow.util.vaadin.LocalizedFormats;
import org.aperteworkflow.util.vaadin.ui.table.ReadOnlyTable;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportResultColumn;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static pl.net.bluesoft.rnd.pt.utils.lang.Lang2.toStringArray;

/**
 * User: POlszewski
 * Date: 2013-08-22
 * Time: 11:40
 */
class ReportResultTable extends ReadOnlyTable<Object[]> {
	private final List<ReportResultColumn> columns;
	private SimpleDateFormat dateFormat;

	protected ReportResultTable(List<ReportResultColumn> columns, I18NSource i18NSource) {
		super(createContainer(columns), i18NSource);
		this.columns = columns;
		this.dateFormat = LocalizedFormats.getShortDateFormat(i18NSource);
		buildLayout("100%");
	}

	private static IndexedContainer createContainer(List<ReportResultColumn> columns) {
		IndexedContainer container = new IndexedContainer();

		for (int i = 0; i < columns.size(); ++i) {
			container.addContainerProperty(String.valueOf(i), String.class, null);
		}
		return container;
	}

	@Override
	protected void addItemsIntoCont(Collection<Object[]> items) {
		String[] propertyNames = getPropertyNames();

		for (Object[] item : items) {
			Item item1 = cont.addItem(item);
			for (int i = 0; i < item.length; ++i) {
				item1.getItemProperty(propertyNames[i]).setValue(format(item[i]));
			}
		}
	}

	private String format(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof Date) {
			return dateFormat.format((Date)value);
		}
		return String.valueOf(value);
	}

	@Override
	protected String[] getPropertyNames() {
		List<String> result = new ArrayList<String>();

		for (int i = 0; i < columns.size(); ++i) {
			result.add(String.valueOf(i));
		}
		return toStringArray(result);
	}

	@Override
	protected String[] getPropertyColumnHeaders() {
		List<String> result = new ArrayList<String>();

		for (int i = 0; i < columns.size(); ++i) {
			result.add(columns.get(i).getDescription());
		}
		return toStringArray(result);
	}

	@Override
	protected Integer[] getPropertyColumnWidths() {
		List<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < columns.size(); ++i) {
			int width = columns.get(i).getWidth();
			result.add(width > 0 ? width : null);
		}
		return result.toArray(new Integer[result.size()]);
	}
}
