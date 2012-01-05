package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;


import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;

import pl.net.bluesoft.rnd.pt.ext.stepeditor.AbstractStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.StepEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.JSONHandler.ParsingFailedException;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.JSONHandler.WidgetNotFoundException;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Window.Notification;

public class UserStepEditorWindow extends AbstractStepEditorWindow implements Handler, ValueChangeListener, ClickListener {

	private static final long		serialVersionUID	= 2136349026207825108L;
	private transient Logger		logger				= Logger.getLogger(UserStepEditorWindow.class.getName());

	private HierarchicalContainer	stepTreeContainer;
	private Tree					stepTree;
	private Tree					availableTree;

	private WidgetItemInStep		rootItem;

	private TextField				nameField;
	private TextArea				commentField;
	private TextField               assigneeField;
	private TextField               candidateGroupsField;
	private TextField               swimlaneField;
	private Label					description;
	
	private Button					saveButton;
	//private Button					closeButton;
	private Button					removeFromStepTreeBtn;
	private boolean					saved;
	
	private WidgetFormWindow		paramPanel;

	private static final Action		ACTION_DELETE		= new Action(Messages.getString("stepTree.action.delete"));
	private static final Action[]	ACTIONS				= new Action[] { ACTION_DELETE };
	private static final Action[]	COMMON_ACTIONS		= new Action[] {};

	
	
	// private static final String FEED_FUNCTION =
	// "window.opener.editorGetData();";

	public UserStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName) {
		super(application, jsonConfig, url, stepName);
	}
	
	public ComponentContainer init() {
		
		ComponentContainer comp = buildLayout();
		
		if (jsonConfig != null && jsonConfig.trim().length() > 0) {
			if (stepTreeContainer != null && rootItem != null) {
				loadJSONConfig();
			}
		}
		
		return comp;
	}
	
    public Label getHeaderLabel() {
		return new Label(Messages.getString("userStep.label"));
    }
	
	private ComponentContainer buildLayout() {
		// setTheme("CHAMELEON");
		// getMainWindow().addListener(this);

		saveButton = new Button(Messages.getString("button.save"), this);
		//closeButton = new Button(Messages.getString("button.close"), this);
		removeFromStepTreeBtn = new Button(Messages.getString("form.delete"), this);
		
		nameField = new TextField(Messages.getString("field.name"));
		nameField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		nameField.setNullRepresentation("");
		commentField = new TextArea(Messages.getString("field.commentary"));
		commentField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		commentField.setNullRepresentation("");
		assigneeField = new TextField(Messages.getString("field.assignee"));
		assigneeField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		assigneeField .setNullRepresentation("");
		candidateGroupsField = new TextField(Messages.getString("field.candidateGroups"));
		candidateGroupsField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		candidateGroupsField .setNullRepresentation("");
		swimlaneField = new TextField(Messages.getString("field.swimlane"));
		swimlaneField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		swimlaneField .setNullRepresentation("");

		availableTree = new Tree(Messages.getString("availableTree.title"), getAvailableItems());
		availableTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
		availableTree.setItemCaptionPropertyId("name");
		availableTree.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		availableTree.setDragMode(TreeDragMode.NODE);
		availableTree.setSelectable(true);
		availableTree.setItemIconPropertyId("icon");
		availableTree.addListener((ValueChangeListener) this);
		availableTree.setImmediate(true);

		stepTree = new Tree(Messages.getString("stepTree.title"), getCurrentStep());
		stepTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
		stepTree.setItemCaptionPropertyId("name");
		stepTree.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		// stepTree.setSizeFull();
		stepTree.setDragMode(TreeDragMode.NODE);
		stepTree.addActionHandler(this);
		stepTree.addListener((ValueChangeListener) this);
		stepTree.setItemIconPropertyId("icon");
		stepTree.setSelectable(true);
		stepTree.setImmediate(true);
		stepTree.setItemDescriptionGenerator(new PropertiesDescriptionGenerator());
		stepTree.expandItemsRecursively(rootItem);

		availableTree.setDropHandler(new TreeDeleteHandler(this, stepTree));
		stepTree.setDropHandler(new TreeDropHandler(stepTree, availableTree));

		description = new Label(Messages.getString("availableTree.description.prompt"));
		description.setContentMode(Label.CONTENT_XHTML);
		description.setSizeFull();

		

		VerticalLayout vlr = new VerticalLayout();
		vlr.setSpacing(true);
		vlr.setMargin(true);
		vlr.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		// vlr.setSizeFull();
		vlr.addComponent(availableTree);
		vlr.addComponent(description);

		HorizontalLayout hll = new HorizontalLayout();
		hll.addComponent(saveButton);
		//hll.addComponent(closeButton);

		VerticalLayout vll = new VerticalLayout();
		vll.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		// vll.setSizeFull();
		vll.addComponent(hll);
		vll.addComponent(nameField);
		vll.addComponent(commentField);
		vll.addComponent(assigneeField);
		vll.addComponent(candidateGroupsField);
		vll.addComponent(swimlaneField);
		vll.addComponent(stepTree);
		vll.addComponent(removeFromStepTreeBtn);
				
		vll.setExpandRatio(hll, 0);
		vll.setExpandRatio(nameField, 0);
		vll.setExpandRatio(commentField, 0);
		vll.setExpandRatio(assigneeField, 0);
		vll.setExpandRatio(candidateGroupsField, 0);
		vll.setExpandRatio(swimlaneField, 0);
		vll.setExpandRatio(stepTree, 9);
		vll.setExpandRatio(removeFromStepTreeBtn, 0);

		

		HorizontalLayout hl = new HorizontalLayout();
		hl.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		// hl.setSizeFull();
		hl.setSpacing(true);
		hl.setMargin(true);

		hl.addComponent(vll);
		hl.addComponent(vlr);
		removeFromStepTreeBtn.setEnabled(stepTree.getValue() != null);
		
		VerticalLayout vl = new VerticalLayout();
		vl.addComponent(hl);
		paramPanel = new WidgetFormWindow();
		vl.addComponent(paramPanel);
		return vl;
	}

	public void deleteTreeItem(final Object widget) {
		ConfirmDialog.show(	application.getMainWindow(),
							Messages.getString("dialog.delete.title"),
							Messages.getString("dialog.delete.question"),
							Messages.getString("dialog.delete.confirm"),
							Messages.getString("dialog.delete.cancel"),
							new ConfirmDialog.Listener() {

								public void onClose(ConfirmDialog dialog) {
									if (dialog.isConfirmed()) {
										HierarchicalContainer hc = (HierarchicalContainer) stepTree.getContainerDataSource();
										hc.removeItemRecursively(widget);
										showParams(null);
										removeFromStepTreeBtn.setEnabled(false);
									} else {

									}
								}
							});
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (target != rootItem)
			return ACTIONS;
		else
			return COMMON_ACTIONS;

	}

	private Container getAvailableItems() {
		HierarchicalContainer hc = getAvailableTreeContainer();

		Map<BundleItem, Collection<WidgetItem>> availableWidgets = new HashMap<BundleItem, Collection<WidgetItem>>();

		try {
			availableWidgets = WidgetInfoLoader.loadAvailableWidgets(application);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			description.setValue(e.getLocalizedMessage() + ": " + e.getStackTrace());
		}

		for (Entry<BundleItem, Collection<WidgetItem>> entry : availableWidgets.entrySet()) {
			final BundleItem bundle = entry.getKey();
			final Collection<WidgetItem> widgets = entry.getValue();

			Item bundleItem = hc.addItem(bundle);
			bundleItem.getItemProperty("name").setValue(bundle.getBundleName());
			bundleItem.getItemProperty("icon").setValue(getResource("icon.bundle.default"));
			hc.setChildrenAllowed(bundle, true);

			for (WidgetItem widgetItem : widgets) {
				Item item = hc.addItem(widgetItem);
				item.getItemProperty("name").setValue(widgetItem.getName());
				item.getItemProperty("icon").setValue(getWidgetIcon(widgetItem));
				hc.setParent(widgetItem, bundle);
				hc.setChildrenAllowed(widgetItem, false);
			}
		}

		hc.sort(new Object[] { "name" }, new boolean[] { true });

		return hc;
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

	private Resource getResource(String path_key) {
		final String path = Messages.getString(path_key);
		final InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
		if (stream != null) {
			String[] path_parts = path.split("/");
			return new StreamResource(new StreamResource.StreamSource() {
				@Override
				public InputStream getStream() {
					return stream;
				}
			}, path_parts[path_parts.length - 1], application);
		}
		return null;
	}

	private Container getCurrentStep() {
		prepareTreeContainer();

		return stepTreeContainer;
	}

	private HierarchicalContainer getAvailableTreeContainer() {
		HierarchicalContainer hc = new HierarchicalContainer();
		hc.addContainerProperty("name", String.class, Messages.getString("availableTree.name.default"));
		hc.addContainerProperty("icon", Resource.class, getResource("icon.widget.default"));
		return hc;
	}

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == ACTION_DELETE) {
			deleteTreeItem(target);
		}
	}

	

	
	private void loadJSONConfig() {
		try {
			Map<String, String> map = JSONHandler.loadConfig(stepTreeContainer, rootItem, jsonConfig);
			if (stepTree != null) {
				stepTree.expandItemsRecursively(rootItem);
			}
			
			nameField.setValue(map.get(JSONHandler.NAME));
			commentField.setValue(map.get(JSONHandler.COMMENTARY));
			assigneeField.setValue(map.get(JSONHandler.ASSIGNEE));
			swimlaneField.setValue(map.get(JSONHandler.SWIMLANE));
			candidateGroupsField.setValue(map.get(JSONHandler.CANDIDATE_GROUPS));
			
			for (Object widget : stepTreeContainer.getItemIds()) {
				if (widget != rootItem)
					stepTree.getItem(widget).getItemProperty("icon").setValue(getWidgetIcon(((WidgetItemInStep) widget).getWidgetItem()));
			}
			//jsonConfig = dumpTreeToJSON();

		} catch (WidgetNotFoundException e) {
			e.printStackTrace();
			application.getMainWindow().showNotification(	Messages.getString("error.config_not_loaded.title"),
												Messages.getString("error.config_not_loaded.widget_not_found.body", e.getWidgetItemName()),
												Notification.TYPE_ERROR_MESSAGE);
		} catch (ParsingFailedException e) {
			e.printStackTrace();
			application.getMainWindow().showNotification(	Messages.getString("error.config_not_loaded.title"),
												Messages.getString("error.config_not_loaded.unexpected_error.body", e.getLocalizedMessage()),
												Notification.TYPE_ERROR_MESSAGE);
		}
	}

	private HierarchicalContainer prepareTreeContainer() {
		stepTreeContainer = new HierarchicalContainer();
		stepTreeContainer.addContainerProperty("name", String.class, Messages.getString("stepTree.name.default"));
		stepTreeContainer.addContainerProperty("icon", Resource.class, getResource("icon.widget.default"));

		final WidgetItem widgetItem = new WidgetItem("ROOT", Messages.getString("stepTree.root.name"), Messages.getString("stepTree.root.description"), null,
				null, null, true, null, null);
		rootItem = new WidgetItemInStep(widgetItem, null, null);
		Item item = stepTreeContainer.addItem(rootItem);
		item.getItemProperty("name").setValue(widgetItem.getName());
		item.getItemProperty("icon").setValue(getResource("icon.root.default"));
		stepTreeContainer.setChildrenAllowed(item, widgetItem.getChildrenAllowed());

		return stepTreeContainer;
	}

	private void showParams(WidgetItemInStep widget) {
		paramPanel.loadWidget(widget, application.getLocale());
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getComponent() == saveButton) {
			save();
			saved = true;
			// closeWindow();
		//} else if (event.getComponent() == closeButton) {
		//	closeWindow();
		} else if (event.getComponent() == removeFromStepTreeBtn) {
			if (stepTree.getValue() != null) {
			  WidgetItemInStep widget = (WidgetItemInStep) stepTree.getValue();
			  deleteTreeItem(widget);
			}
		}
	}

	private void save() {
		//jsonConfig = dumpTreeToJSON();
		application.getJsHelper().postAndRedirect(url, dumpTreeToJSON());

	}

	/*private void closeWindow() {
		if (is_saved()) {
			application.getJsHelper().allowWindowClosing();
		} else {
			application.getJsHelper().preventWindowClosing();
		}
		application.getJsHelper().closeWindow(url);
	}*/
	
	private String dumpTreeToJSON() {
		return JSONHandler.dumpTreeToJSON(stepTree, rootItem, nameField.getValue(), commentField.getValue(), assigneeField.getValue(), candidateGroupsField.getValue(), swimlaneField.getValue(), stepName);
	}

	// @Override
	// public void windowClose(CloseEvent e) {
	// if (is_saved()) {
	// getMainWindow().executeJavaScript(CLOSE_ALLOW_FUNCTION);
	// } else {
	// getMainWindow().executeJavaScript(CLOSE_PREVENT_FUNCTION);
	// }
	// }

	//private boolean is_saved() {
		//String tmpJSONConfig = dumpTreeToJSON();
		//return tmpJSONConfig.equals(jsonConfig);
	//}

	@Override
	public void valueChange(ValueChangeEvent event) {
		
		removeFromStepTreeBtn.setEnabled(stepTree.getValue() != null && stepTree.getValue() != rootItem);
		
		if (event.getProperty() == stepTree) {
			if (stepTree.getValue() == null || stepTree.getValue() == rootItem) {
			  showParams(null);
			} else {
			  WidgetItemInStep widget = (WidgetItemInStep) stepTree.getValue();
			  stepTree.setValue(widget);
			  showParams(widget);
			}
		} else if (event.getProperty() == availableTree) {
			if (availableTree.getValue() instanceof BundleItem) {
				BundleItem bundle = (BundleItem) availableTree.getValue();
				if (StringUtils.isNotEmpty(bundle.getBundleDescription()))
					description.setValue(bundle.getBundleDescription());
				else
					description.setValue(Messages.getString("availableTree.group.description", bundle.getBundleName()));

			} else if (availableTree.getValue() instanceof WidgetItem) {
				WidgetItem widget = (WidgetItem) availableTree.getValue();
				description.setValue(widget.getDescription());
			}
		}
	}
	
	
}
