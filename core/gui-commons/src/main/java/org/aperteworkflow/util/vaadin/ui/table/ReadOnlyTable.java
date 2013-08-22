package org.aperteworkflow.util.vaadin.ui.table;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Table;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

/**
 * User: POlszewski
 * Date: 2012-09-02
 * Time: 14:53
 */
public abstract class ReadOnlyTable<T> extends Table implements Table.CellStyleGenerator {
	protected final Container cont;
	protected final I18NSource i18NSource;

	protected ReadOnlyTable(Class<T> itemClass, I18NSource i18NSource) {
		this(new BeanItemContainer<T>(itemClass), i18NSource);
	}

	protected ReadOnlyTable(Container cont, I18NSource i18NSource) {
		this.cont = cont;
		this.i18NSource = i18NSource;
	}

	protected void buildLayout(String width) {
		if (width != null) {
			setWidth(width);
		}
		else {
			setSizeUndefined();
		}
		setPageLength(0);
		setEditable(false);
		setContainerDataSource(cont);
		String[] propertyNames = getPropertyNames();
		addGeneratedColumns();
		setVisibleColumns(propertyNames);
		setColumnHeaders(getPropertyColumnHeaders());
		setCellStyleGenerator(this);
		Integer[] widths = getPropertyColumnWidths();
		for (int i = 0; i < propertyNames.length; ++i) {
			if (widths[i] != null) {
				setColumnWidth(propertyNames[i], widths[i]);
			}
		}
		adjustPageLength();
		addListener(new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				handleItemSelection((T)event.getItemId());
			}
		});
	}

	protected void addGeneratedColumns() {
	}

	protected abstract String[] getPropertyNames();
	protected abstract String[] getPropertyColumnHeaders();
	protected abstract Integer[] getPropertyColumnWidths();

	public void setItems(Collection<T> items) {
		cont.removeAllItems();
		addItemsIntoCont(items);
		adjustPageLength();
	}

	protected void addItemsIntoCont(Collection<T> items) {
		if (items != null) {
			if (cont instanceof BeanItemContainer) {
				((BeanItemContainer)cont).addAll(items);
			}
			else {
				for (T item : items) {
					cont.addItem(item);
				}
			}
		}
	}

	public Collection<T> getItems() {
		return (Collection)cont.getItemIds();
	}

	protected void adjustPageLength() {
		setPageLength(Math.min(getMaxDisplayedItems(), cont.size()));
	}

	private int getMaxDisplayedItems() {
		return 15;
	}

	@Override
	protected final String formatPropertyValue(Object rowId, Object colId, Property property) {
		String value = formatPropertyValue((T)rowId, (String)colId);
		return value != null ? value : super.formatPropertyValue(rowId, colId, property);
	}

	protected String formatPropertyValue(T item, String property) {
		return null;
	}

	@Override
	public final String getStyle(Object itemId, Object propertyId) {
		return getStyle((T)itemId, (String)propertyId);
	}

	protected String getStyle(T item, String property) {
		return null;
	}

	protected void handleItemSelection(T item) {
	}

	protected String getMessage(String key) {
		return i18NSource.getMessage(key);
	}
}
