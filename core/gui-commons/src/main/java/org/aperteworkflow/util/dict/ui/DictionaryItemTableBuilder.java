package org.aperteworkflow.util.dict.ui;

import com.vaadin.Application;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemValueWrapper;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemWrapper;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.aperteworkflow.util.vaadin.ui.table.LocalizedPagedTable;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Strings;

import java.text.DateFormat;
import java.util.*;

import static org.aperteworkflow.util.vaadin.VaadinUtility.*;

/**
 * User: POlszewski
 * Date: 2012-12-03
 * Time: 11:16
 */
public abstract class DictionaryItemTableBuilder<
		WrappedItemType,
		ItemValueWrapperType extends DictionaryItemValueWrapper,
		DictionaryItemWrapperType extends DictionaryItemWrapper<WrappedItemType, ItemValueWrapperType>
	> {
	private static final String EMPTY_VALID_DATE = "...";
	public static final String _KEY = "key";
	public static final String _DESCRIPTION = "description";
	public static final String GEN_VALUE = "value";
	public static final String GEN_EXTENSIONS = "extensions";
	public static final String GEN_DELETE = "delete";

	private DictionaryItemModificationHandler<DictionaryItemWrapperType> handler;

	private Window detailsWindow = null;

	public DictionaryItemTableBuilder(DictionaryItemModificationHandler<DictionaryItemWrapperType> handler) {
		this.handler = handler;
	}

	public interface SaveCallback<DictionaryItemWrapperType extends DictionaryItemWrapper> {
		void onSave(BeanItem<DictionaryItemWrapperType> item);
	}

	public interface DictionaryItemModificationHandler<DictionaryItemWrapperType extends DictionaryItemWrapper> {
		void handleItemSave(DictionaryItemWrapperType item);
		void handleItemDelete(DictionaryItemWrapperType item);
	}

	private class DictPopupView extends PopupView {
		private Label info;

		public DictPopupView(final String smallTitle, DictPopupVisibilityListener listener) {
			super(smallTitle, null);
			this.info = new Label("", Label.CONTENT_XHTML);
			this.info.setWidth(400, UNITS_PIXELS);
			setContent(new Content() {
				@Override
				public String getMinimizedValueAsHTML() {
					return smallTitle;
				}

				@Override
				public Component getPopupComponent() {
					return info;
				}
			});
			listener.setLargeView(info);
			addListener(listener);
			setHideOnMouseOut(true);
			addStyleName("bubble");
		}
	}

	private abstract class DictPopupVisibilityListener<
			WrappedItemType,
			ItemValueWrapperType extends DictionaryItemValueWrapper,
			DictionaryItemWrapperType extends DictionaryItemWrapper<WrappedItemType, ItemValueWrapperType>
		> implements PopupView.PopupVisibilityListener {
		private final BeanItemContainer<DictionaryItemWrapperType> container;
		private final Object itemId;
		private Label largeView;

		public DictPopupVisibilityListener(BeanItemContainer<DictionaryItemWrapperType> container, Object itemId) {
			this.container = container;
			this.itemId = itemId;
		}

		public void setLargeView(Label largeView) {
			this.largeView = largeView;
		}

		public abstract String getEmptyDescription();

		public abstract String getItemRepresentation(ItemValueWrapperType item);

		@Override
		public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
			if (event.isPopupVisible()) {
				DictionaryItemWrapperType item = container.getItem(itemId).getBean();
				List<ItemValueWrapperType> values = new ArrayList<ItemValueWrapperType>(item.getValues());
				StringBuilder sb = new StringBuilder();
				if (values.isEmpty()) {
					sb.append(getEmptyDescription());
				}
				else {
					sb.append("<ul>");
					Collections.sort(values, new Comparator<ItemValueWrapperType>() {
						@Override
						public int compare(ItemValueWrapperType o1, ItemValueWrapperType o2) {
							return o1.getValue().compareTo(o2.getValue());
						}
					});
					for (ItemValueWrapperType value : values) {
						sb.append("<li>").append(getItemRepresentation(value)).append("</li>");
					}
					sb.append("</ul>");
				}
				sb.append("</b>");
				largeView.setValue(sb.toString());
			}
		}
	}

	public Component createTable(final BeanItemContainer<DictionaryItemWrapperType> container) {
		Map<String, Table.ColumnGenerator> customColumns = new HashMap<String, Table.ColumnGenerator>();
		customColumns.put(GEN_VALUE, createValueColumn(container));
		customColumns.put(GEN_EXTENSIONS, createAdditionalValuesColumn(container));
		customColumns.put(GEN_DELETE, createDeleteColumn(container));

		String[] visibleColumns = new String[] { _KEY, _DESCRIPTION, GEN_VALUE, GEN_EXTENSIONS, GEN_DELETE };
		String[] columnHeaders = new String[] {
				getMessage("dict.item.key"), getMessage("dict.item.description"),
				getMessage("dict.item.values"), getMessage("dict.item.extensions"),
				getMessage("pagedtable.delete")
		};

		LocalizedPagedTable table = pagedTable(container, visibleColumns, columnHeaders, customColumns, new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				showItemDetails(container.getItem(event.getItemId()), new SaveCallback<DictionaryItemWrapperType>() {
					@Override
					public void onSave(BeanItem<DictionaryItemWrapperType> item) {
						handler.handleItemSave(item.getBean());
						closeDetailsWindow();
					}
				});
			}
		});
		return wrapPagedTable(getI18NSource(), table);
	}

	private Table.ColumnGenerator createValueColumn(final BeanItemContainer<DictionaryItemWrapperType> container) {
		return new Table.ColumnGenerator() {
			@Override
			public Component generateCell(Table source, final Object itemId, Object columnId) {
				return new DictPopupView(getMessage("dict.showValues"), new DictPopupVisibilityListener<WrappedItemType, ItemValueWrapperType, DictionaryItemWrapperType>(container, itemId) {
					@Override
					public String getEmptyDescription() {
						return getMessage("dict.item.novalues");
					}
					@Override
					public String getItemRepresentation(ItemValueWrapperType item) {
						DateFormat dateFormat = VaadinUtility.simpleDateFormat();
						StringBuilder sb = new StringBuilder().append("<b>").append(item.getValue()).append("</b>").append(" (").append("<i>");
						if (item.hasFullDatesRange()) {
							sb.append(getMessage("dict.full.range"));
						}
						else {
							sb.append(item.getValidStartDate() != null ? dateFormat.format(item.getValidStartDate()) : EMPTY_VALID_DATE)
									.append(" - ")
									.append(item.getValidEndDate() != null ? dateFormat.format(item.getValidEndDate()) : EMPTY_VALID_DATE);
						}
						sb.append("</i>)");
						return sb.toString();
					}
				});
			}
		};
	}

	private Table.ColumnGenerator createAdditionalValuesColumn(final BeanItemContainer<DictionaryItemWrapperType> container) {
		return new Table.ColumnGenerator() {
			@Override
			public Component generateCell(Table source, final Object itemId, Object columnId) {
				return new DictPopupView(getMessage("dict.showExtensions"), new DictPopupVisibilityListener<WrappedItemType, ItemValueWrapperType, DictionaryItemWrapperType>(container, itemId) {
					@Override
					public String getEmptyDescription() {
						return getMessage("dict.item.noextensions");
					}

					@Override
					public String getItemRepresentation(ItemValueWrapperType item) {
						StringBuilder sb = new StringBuilder().append("<b>").append(item.getValue()).append("</b>").append("<ul>");
						List<String> extensionNames = new ArrayList<String>(item.getExtensionNames());
						if (extensionNames.isEmpty()) {
							sb.append("<li>").append(getMessage("dict.item.noextensions")).append("</li>");
						}
						else {
							Collections.sort(extensionNames);
							for (String extensionName : extensionNames) {
								DictionaryItemExtensionWrapper ext = item.getExtensionByName(extensionName);
								sb.append("<li>")
										.append("<b>").append(ext.getName()).append("</b>")
										.append(Strings.hasText(ext.getDescription())
												? " (" + ext.getDescription() + ")" : "")
										.append(": ")
										.append(Strings.hasText(ext.getValue()) ? "<b>" + ext.getValue() + "</b>"
												: getMessage("dict.item.extensions.novalue"))
										.append("</li>");
							}
						}
						sb.append("</ul>");
						return sb.toString();
					}
				});
			}
		};
	}

	private Table.ColumnGenerator createDeleteColumn(final BeanItemContainer<DictionaryItemWrapperType> container) {
		return new Table.ColumnGenerator() {
			@Override
			public Component generateCell(Table source, final Object itemId, Object columnId) {
				Button b = smallButton(getMessage("pagedtable.delete"));
				b.addListener(new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						DictionaryItemWrapperType item = container.getItem(itemId).getBean();
						container.removeItem(itemId);
						handler.handleItemDelete(item);
					}
				});
				return b;
			}
		};
	}

	public void showItemDetails(final BeanItem<DictionaryItemWrapperType> item, final SaveCallback<DictionaryItemWrapperType> callback) {
		if (getDetailsWindow() != null) {
			return;
		}

		final DictionaryItemForm form = createDictionaryItemForm(getApplication(), getI18NSource(), item);
		form.setWidth("100%");

		form.addSaveClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				Map<Field, String> messages = new LinkedHashMap<Field, String>();
				for (Object propertyId : form.getItemPropertyIds()) {
					Field field = form.getField(propertyId);
					try {
						field.validate();
					}
					catch (Validator.InvalidValueException e) {
						messages.put(field, e.getMessage());
					}
				}
				if (messages.isEmpty()) {
					form.commit();
					callback.onSave(item);
				}
				else {
					StringBuilder sb = new StringBuilder();
					for (String msg : messages.values()) {
						sb.append(msg).append("<br/>");
					}
					validationNotification(getApplication(), getI18NSource(), sb.toString());
				}
			}
		});

		form.addCancelClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				form.discard();
				closeDetailsWindow();
			}
		});

		wrapWithModalWindow(form);
		showDetailsWindow();
	}

	protected abstract DictionaryItemForm createDictionaryItemForm(Application application, I18NSource source, BeanItem<DictionaryItemWrapperType> item);

	public void showDetailsWindow() {
		getApplication().getMainWindow().addWindow(getDetailsWindow());
	}

	public void closeDetailsWindow() {
		getApplication().getMainWindow().removeWindow(getDetailsWindow());
		setDetailsWindow(null);
	}

	private void wrapWithModalWindow(Form form) {
		Panel panel = new Panel();
		panel.setWidth("800px");
		panel.setScrollable(true);
		panel.addComponent(form);
		setDetailsWindow(modalWindow(getMessage("dict.item"), panel));
	}

	private String getMessage(String key) {
		return getI18NSource().getMessage(key);
	}

	protected abstract Application getApplication();
	protected abstract I18NSource getI18NSource();

	private Window getDetailsWindow() {
		return detailsWindow;
	}

	private void setDetailsWindow(Window detailsWindow) {
		this.detailsWindow = detailsWindow;
	}
}
