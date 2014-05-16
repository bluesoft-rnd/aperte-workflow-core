package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.vaadin.dialogs.ConfirmDialog;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.sort;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * User: POlszewski
 * Date: 2014-05-15
 */
public class WidgetEditor implements Property.ValueChangeListener, Action.Handler {
	private static final Logger logger = Logger.getLogger(WidgetEditor.class.getName());

	private static final Action[] COMMON_ACTIONS = {};
	private Action[] actions;
	private Action actionDelete;

	private final Application application;
	private HierarchicalContainer stepTreeContainer;
	private Component availableWidgetsPane;
	private Tree stepTree;
	private WidgetFormWindow paramPanel;

	private WidgetItemInStep rootItem;

	public WidgetEditor(Application application) {
		this.application = application;
		this.actionDelete = new Action(I18NSource.ThreadUtil.getThreadI18nSource().getMessage("stepTree.action.delete"));
		this.actions = new Action[] { actionDelete };
	}

	public void init() {
		prepareAvailableWidgetsComponent();
		initStepTree();
	}

	public boolean isInitialized() {
		return stepTreeContainer != null && rootItem != null;
	}

	private HierarchicalContainer prepareTreeContainer() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		stepTreeContainer = new HierarchicalContainer();
		stepTreeContainer.addContainerProperty("name", String.class, messages.getMessage("stepTree.name.default"));
		stepTreeContainer.addContainerProperty("icon", Resource.class, getResource("icon.widget.default"));

		WidgetItem widgetItem = new WidgetItem(
				"ROOT",
				messages.getMessage("stepTree.root.name"),
				messages.getMessage("stepTree.root.description"),
				null,
				null,
				new ArrayList<PermissionDefinition>(),
				true,
				null
		);
		rootItem = new WidgetItemInStep(widgetItem, null, null, null);
		Item item = stepTreeContainer.addItem(rootItem);
		item.getItemProperty("name").setValue(widgetItem.getName());
		item.getItemProperty("icon").setValue(getResource("icon.root.default"));
		stepTreeContainer.setChildrenAllowed(item, widgetItem.getChildrenAllowed());

		return stepTreeContainer;
	}

	private void prepareAvailableWidgetsComponent() {
		List<WidgetItem> availableWidgetItems = getAvailableWidgetItems();
		sort(availableWidgetItems, new Comparator<WidgetItem>() {
			@Override
			public int compare(WidgetItem o1, WidgetItem o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		CssLayout pane = new CssLayout() {
			@Override
			protected String getCss(Component component) {
				if (component instanceof WidgetInfoDnDWrapper) {
					WidgetInfoDnDWrapper wrapper = (WidgetInfoDnDWrapper) component;
					String basicCss = "float: left; margin: 3px; padding: 3px;  display: inline; font-weight: bold; border: 2px solid ";
					return basicCss +  (nvl(wrapper.widgetItem.getChildrenAllowed(), false)
							? "#287ece;" : "#60b30e;");
				}
				return super.getCss(component);
			}
		};
		pane.setWidth("100%");
		for (WidgetItem wi : availableWidgetItems) {
			Label lbl = new Label(wi.getName());
			lbl.setDescription(wi.getDescription());
			lbl.setSizeUndefined();
			DragAndDropWrapper c = new WidgetInfoDnDWrapper(lbl, wi);
			c.setSizeUndefined();
			pane.addComponent(c);
			c.setDragStartMode(DragAndDropWrapper.DragStartMode.WRAPPER);
		}

		DragAndDropWrapper wr = new DragAndDropWrapper(pane);
		wr.setDropHandler(new DropHandler() {
			@Override
			public void drop(DragAndDropEvent event) {
				Transferable t = event.getTransferable();
				Component src = t.getSourceComponent();
				if (src != stepTree || !(t instanceof DataBoundTransferable)) {
					return;
				}
				Object sourceItemId = ((DataBoundTransferable) t).getItemId();

				stepTreeContainer.removeItemRecursively(sourceItemId);
			}
			@Override
			public AcceptCriterion getAcceptCriterion() {
				return AcceptAll.get();
			}
		});
		this.availableWidgetsPane = wr;
	}

	private List<WidgetItem> getAvailableWidgetItems() {
		List<WidgetItem> widgetItems = new ArrayList<WidgetItem>();
		Map<BundleItem, Collection<WidgetItem>> availableWidgets = new HashMap<BundleItem, Collection<WidgetItem>>();
		try {
			availableWidgets = WidgetInfoLoader.loadAvailableWidgets();
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Error loading available widgets", e);
		}
		for (Map.Entry<BundleItem, Collection<WidgetItem>> entry : availableWidgets.entrySet()) {
			Collection<WidgetItem> widgets = entry.getValue();
			for (WidgetItem widgetItem : widgets) {
				widgetItems.add(widgetItem);
			}
		}
		return widgetItems;
	}

	public VerticalLayout buildWidgetEditorTabContent() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		VerticalLayout availableWidgetsLayout = new VerticalLayout();
		availableWidgetsLayout.setSpacing(true);
		availableWidgetsLayout.setWidth("100%");
		availableWidgetsLayout.addComponent(availableWidgetsPane);
		VerticalLayout stepLayout = new VerticalLayout();
		stepLayout.setWidth("100%");
		stepLayout.setSpacing(true);
		stepLayout.setMargin(true);
		stepLayout.addComponent(new Label(messages.getMessage("userstep.editor.widgets.instructions"), Label.CONTENT_XHTML));
		stepLayout.addComponent(availableWidgetsLayout);
		Panel treePanel = new Panel();
		treePanel.setStyleName(Reindeer.PANEL_LIGHT);
		treePanel.addComponent(stepTree);
		treePanel.setWidth("245px");

		HorizontalLayout treeAndParamLayout = new HorizontalLayout();
		treeAndParamLayout.setWidth("100%");
		treeAndParamLayout.setSpacing(true);
		treeAndParamLayout.addComponent(treePanel);
		treeAndParamLayout.addComponent(paramPanel);
		treeAndParamLayout.setExpandRatio(paramPanel, 1.0f);
		stepLayout.addComponent(treeAndParamLayout);
		stepLayout.setExpandRatio(treeAndParamLayout, 1.0f);
		return stepLayout;
	}

	private void initStepTree() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

		stepTree = new Tree(messages.getMessage("stepTree.title"), getCurrentStep());
		stepTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
		stepTree.setItemCaptionPropertyId("name");
		stepTree.setDragMode(Tree.TreeDragMode.NODE);
		stepTree.addActionHandler(this);
		stepTree.addListener(this);
		stepTree.setSelectable(true);
		stepTree.setImmediate(true);
		stepTree.setItemDescriptionGenerator(new PropertiesDescriptionGenerator());
		stepTree.expandItemsRecursively(rootItem);
		stepTree.setDropHandler(new TreeDropHandler(stepTree, stepTreeContainer));

		paramPanel = new WidgetFormWindow();
	}

	private void deleteTreeItem(final Object widget) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		ConfirmDialog.show(application.getMainWindow(),
				messages.getMessage("dialog.delete.title"),
				messages.getMessage("dialog.delete.question"),
				messages.getMessage("dialog.delete.confirm"),
				messages.getMessage("dialog.delete.cancel"),
				new ConfirmDialog.Listener() {
					@Override
					public void onClose(ConfirmDialog dialog) {
						if (dialog.isConfirmed()) {
							HierarchicalContainer hc = (HierarchicalContainer)stepTree.getContainerDataSource();
							hc.removeItemRecursively(widget);
							showParams(null);
						}
					}
				}
		);
	}

	@Override
	public void valueChange(Property.ValueChangeEvent event) {
		if (event.getProperty() == stepTree) {
			if (stepTree.getValue() == null || stepTree.getValue() == rootItem) {
				showParams(null);
			} else {
				WidgetItemInStep widget = (WidgetItemInStep)stepTree.getValue();
				stepTree.setValue(widget);
				showParams(widget);
			}
		}
	}

	private void showParams(WidgetItemInStep widget) {
		paramPanel.loadWidget(widget, true);
	}

	private Resource getWidgetIcon(WidgetItem widgetItem) {
		try {
			final InputStream stream = widgetItem.getBundle().getIconStream(widgetItem.getIcon());

			if (stream != null)
				return new StreamResource(new StreamResource.StreamSource() {
					@Override
					public InputStream getStream() {
						return stream;
					}
				}, widgetItem.getIcon(), application);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Icon \"" + widgetItem.getIcon() + "\" for widget \"" + widgetItem.getName() + "\" not found!");
		}

		return getResource("icon.widget.default");
	}

	private Resource getResource(String pathKey) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		String path = messages.getMessage(pathKey);
		final InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
		if (stream != null) {
			String[] pathParts = path.split("/");
			return new StreamResource(new StreamResource.StreamSource() {
				@Override
				public InputStream getStream() {
					return stream;
				}
			}, pathParts[pathParts.length - 1], application);
		}
		return null;
	}

	private Container getCurrentStep() {
		prepareTreeContainer();

		return stepTreeContainer;
	}

	public void loadJSONConfig() {
		for (Object widget : stepTreeContainer.getItemIds()) {
			if (widget != rootItem) {
				stepTree.getItem(widget).getItemProperty("icon").setValue(getWidgetIcon(((WidgetItemInStep) widget).getWidgetItem()));
			}
		}
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (target != rootItem) {
			return actions;
		} else {
			return COMMON_ACTIONS;
		}
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == actionDelete) {
			deleteTreeItem(target);
		}
	}

	public void expandRecursively() {
		if (stepTree != null) {
			stepTree.expandItemsRecursively(rootItem);
		}
	}

	public void switchToProblematicWidget(WidgetItemInStep widgetInStep) {
		// switch to problematic widget
		paramPanel.loadWidget(widgetInStep, false);
		stepTree.setValue(widgetInStep);
	}

	public Tree getStepTree() {
		return stepTree;
	}

	public WidgetItemInStep getRootItem() {
		return rootItem;
	}

	public HierarchicalContainer getStepTreeContainer() {
		return stepTreeContainer;
	}
}
