package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ListSelect;
import org.aperteworkflow.util.vaadin.ui.Dialog;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Pair;

import java.util.List;
import java.util.Set;

/**
 * User: POlszewski
 * Date: 2012-10-14
 * Time: 16:34
 */
public abstract class SelectValuesDialog<ItemType> extends Dialog {
	private Class<ItemType> itemClass;
	private I18NSource i18NSource;

	private ListSelect itemSelect;

	public SelectValuesDialog(Class<ItemType> itemClass, String title, I18NSource i18NSource) {
		super(title);
		this.itemClass = itemClass;
		this.i18NSource = i18NSource;
		buildDialogLayout();
	}

	private void buildDialogLayout() {
		itemSelect = new ListSelect();
		itemSelect.setWidth("100%");
		itemSelect.setNullSelectionAllowed(false);
		itemSelect.setMultiSelect(true);
		itemSelect.setContainerDataSource(new BeanItemContainer<ItemType>(itemClass));
		itemSelect.setRows(10);
		itemSelect.setImmediate(true);

		addDialogContent(itemSelect);

		addDialogAction(getMessage("Wybierz"), new ActionListener() {
			@Override
			public void handleAction(String action) {
				valuesSelected((Set<ItemType>)itemSelect.getValue());
			}
		});
		addDialogAction(getMessage("Anuluj"), null);
	}

	protected abstract void valuesSelected(Set<ItemType> items);

	private String getMessage(String key) {
		return i18NSource.getMessage(key);
	}

	public void setAvailableItems(List<Pair<String, ItemType>> items) {
		itemSelect.removeAllItems();
		for (Pair<String, ItemType> item : items) {
			itemSelect.addItem(item.getSecond());
			itemSelect.setItemCaption(item.getSecond(), item.getFirst());
		}
	}

	public void setItems(Set<ItemType> items) {
		itemSelect.setValue(items);
	}
}
