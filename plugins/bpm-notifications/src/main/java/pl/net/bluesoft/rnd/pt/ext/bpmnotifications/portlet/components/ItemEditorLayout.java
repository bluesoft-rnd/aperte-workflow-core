package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Lang;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.Collection;
import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:29
 */
public abstract class ItemEditorLayout<ItemType extends PersistentEntity> extends HorizontalLayout
		implements DataLoadable, Button.ClickListener, Property.ValueChangeListener {
	private Class<ItemType> itemClass;
	private I18NSource i18NSource;
	private ProcessToolRegistry registry;

	private ListSelect itemSelect;
	private Button saveButton;
	private Button newButton;

	private Long itemId;

	public ItemEditorLayout(Class<ItemType> itemClass, I18NSource i18NSource, ProcessToolRegistry registry) {
		this.itemClass = itemClass;
		this.i18NSource = i18NSource;
		this.registry = registry;
	}

	protected void buildLayout() {
		setWidth("100%");
		setSpacing(true);

		itemSelect = new ListSelect();
		itemSelect.setWidth("250px");
		itemSelect.setNullSelectionAllowed(false);
		itemSelect.setContainerDataSource(new BeanItemContainer<ItemType>(itemClass));
		itemSelect.setRows(20);
		itemSelect.setImmediate(true);
		itemSelect.addListener(this);

		Component itemDetails = createItemDetailsLayout();
		Component infoLayout = createInfoLayout();
		saveButton = new Button(getMessage("Zapisz"));
		saveButton.addListener((Button.ClickListener)this);
		newButton = new Button(getMessage("Nowy"));
		newButton.addListener((Button.ClickListener)this);

		VerticalLayout rightLayout = new VerticalLayout();
		rightLayout.setSpacing(true);
		rightLayout.addComponent(itemDetails);
		if (infoLayout != null) {
			rightLayout.addComponent(infoLayout);
		}
		rightLayout.addComponent(hl(saveButton, newButton));

		addComponent(itemSelect);
		addComponent(rightLayout);
		setExpandRatio(rightLayout, 1);
	}

	protected Component createInfoLayout() {
		return null;
	}

	protected abstract Component createItemDetailsLayout();
	protected abstract void clearDetails();
	protected abstract void loadDetails(ItemType item);
	protected abstract void saveDetails(ItemType item);

	protected void prepareData() {
	}

	protected abstract List<ItemType> getAllItems();
	protected abstract String getItemCaption(ItemType item);
	protected abstract ItemType createItem();
	protected abstract ItemType refreshItem(Long id);
	protected abstract void saveItem(ItemType item);

	@Override
	public void buttonClick(Button.ClickEvent event) {
		if (event.getSource() == saveButton) {
			saveData();
		}
		else if (event.getSource() == newButton) {
			itemSelect.setValue(null);
			loadDetails(createItem());
		}
	}

	@Override
	public void loadData() {
		prepareData();
		doLoadData(null);
	}

	private void doLoadData(Long selectedItemId) {
		itemSelect.removeAllItems();
		List<ItemType> allItems = from(getAllItems()).orderBy(new F<ItemType, Comparable>() {
			@Override
			public Comparable invoke(ItemType x) {
				return getItemCaption(x);
			}
		}).toList();
		ItemType selectedItem = null;
		for (ItemType item : allItems) {
			itemSelect.addItem(item);
			itemSelect.setItemCaption(item, getItemCaption(item));
			if (Lang.equals(selectedItemId, item.getId())) {
				selectedItem = item;
			}
		}
		if (selectedItem != null) {
			itemSelect.setValue(selectedItem);
		}
		else if (!allItems.isEmpty()) {
			itemSelect.setValue(allItems.get(0));
		}
	}

	private void saveData() {
		ItemType item;

		if (itemId != null) {
			item = refreshItem(itemId);
		}
		else {
			item = createItem();
		}
		saveDetails(item);
		saveItem(item);
		getService().invalidateCache();
		doLoadData(item.getId());
	}

	@Override
	public void valueChange(Property.ValueChangeEvent event) {
		ItemType value = (ItemType)itemSelect.getValue();
		if (value != null) {
			itemId = value.getId();
			loadDetails(value);
		}
		else {
			itemId = null;
			clearDetails();
		}
	}

	protected String getMessage(String key) {
		return key != null ? i18NSource.getMessage(key) : key;
	}

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	protected BpmNotificationService getService() {
		return registry.getRegisteredService(BpmNotificationService.class);
	}

	protected TextField textField(String caption, int width) {
		TextField textField = textField(caption);
		if (width >= 0) {
			textField.setWidth(width, UNITS_PIXELS);
		}
		else {
			textField.setWidth(100, UNITS_PERCENTAGE);
		}
		return textField;
	}

	protected TextField textField(String caption) {
		TextField textField = new TextField(getMessage(caption));
		textField.setNullRepresentation("");
		return textField;
	}

	protected TextArea textArea(String caption, int width) {
		TextArea textArea = textArea(caption);
		if (width >= 0) {
			textArea.setWidth(width, UNITS_PIXELS);
		}
		else {
			textArea.setWidth(100, UNITS_PERCENTAGE);
		}
		return textArea;
	}

	protected TextArea textArea(String caption) {
		TextArea textArea = new TextArea(getMessage(caption));
		textArea.setNullRepresentation("");
		return textArea;
	}

	protected CheckBox checkBox(String caption) {
		return new CheckBox(getMessage(caption));
	}

	protected Select select(String caption, int width) {
		Select select = select(caption);
		if (width >= 0) {
			select.setWidth(width, UNITS_PIXELS);
		}
		else {
			select.setWidth(100, UNITS_PERCENTAGE);
		}
		return select;
	}

	protected Select select(String caption) {
		return new Select(getMessage(caption));
	}

	protected void bindValues(Select select, Collection<String> values) {
		select.removeAllItems();
		for (String value : values) {
			if (value != null) {
				select.addItem(value);
			}
		}
	}

	protected String getString(Field field) {
		return (String)field.getValue();
	}

	protected boolean getBoolean(CheckBox field) {
		return field.booleanValue();
	}

	protected HorizontalLayout hl(String caption, Component... components) {
		HorizontalLayout hl = hl(components);
		for (Component component : components) {
			hl.setComponentAlignment(component, Alignment.MIDDLE_LEFT);
		}
		hl.setCaption(getMessage(caption));
		return hl;
	}

	protected HorizontalLayout hl(Component... components) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		for (Component component : components) {
			hl.addComponent(component);
		}
		return hl;
	}
}
