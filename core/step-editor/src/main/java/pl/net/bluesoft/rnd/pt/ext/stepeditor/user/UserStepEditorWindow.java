package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;


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
import org.apache.commons.lang.StringUtils;
import org.vaadin.dialogs.ConfirmDialog;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.AbstractStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.StepEditorApplication;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.JSONHandler.ParsingFailedException;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.JSONHandler.WidgetNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserStepEditorWindow extends AbstractStepEditorWindow implements Handler, ValueChangeListener, ClickListener {

	private static final long		serialVersionUID	= 2136349026207825108L;
	private static final Logger		logger				= Logger.getLogger(UserStepEditorWindow.class.getName());

	private HierarchicalContainer	stepTreeContainer;
	private Tree					stepTree;
    private Label                   stepTreeHintLabel;
	private Tree					availableTree;
    private Label                   availableTreeHintLabel;

	private WidgetItemInStep		rootItem;
    
	private TextField               assigneeField;
	private TextField               candidateGroupsField;
	private TextField               swimlaneField;
	private Label					description;
	
	private Button					saveButton;
	private Button                  removeFromStepTreeButton;
	private boolean					saved;
	
	private WidgetFormWindow		paramPanel;

	private static final Action		ACTION_DELETE		= new Action(Messages.getString("stepTree.action.delete"));
	private static final Action[]	ACTIONS				= new Action[] { ACTION_DELETE };
	private static final Action[]	COMMON_ACTIONS		= new Action[] {};

	public UserStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
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

    @Override
    public Component getHeader() {
    	Label headerLabel = new Label();
        headerLabel.setContentMode(Label.CONTENT_XHTML);

        if (stepName != null && !stepName.isEmpty()) {
            headerLabel.setValue("<h2>" + Messages.getString("userStep.stepName", stepName) + "</h2>");
        } else {
            headerLabel.setValue("<h2>" + Messages.getString("userStep.noStepName") + "</h2>");
        }

        return headerLabel;
    }
	
	private ComponentContainer buildLayout() {
		// setTheme("CHAMELEON");
		// getMainWindow().addListener(this);

		saveButton = new Button(Messages.getString("button.save"), this);
		removeFromStepTreeButton = new Button(Messages.getString("form.delete"), this);

		assigneeField = new TextField(Messages.getString("field.assignee"));
        assigneeField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		assigneeField.setNullRepresentation("");
		candidateGroupsField = new TextField(Messages.getString("field.candidateGroups"));
        candidateGroupsField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		candidateGroupsField .setNullRepresentation("");
		swimlaneField = new TextField(Messages.getString("field.swimlane"));
        swimlaneField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		swimlaneField.setNullRepresentation("");

		availableTree = new Tree(Messages.getString("availableTree.title"), getAvailableItems());
        availableTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
        availableTree.setItemCaptionPropertyId("name");
        availableTree.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        availableTree.setDragMode(TreeDragMode.NODE);
		availableTree.setSelectable(true);
        availableTree.setItemIconPropertyId("icon");
        availableTree.addListener((ValueChangeListener) this);
		availableTree.setImmediate(true);

        availableTreeHintLabel = new Label(Messages.getString("availableTree.hint"));
        
		stepTree = new Tree(Messages.getString("stepTree.title"), getCurrentStep());
        stepTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
        stepTree.setItemCaptionPropertyId("name");
        stepTree.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		stepTree.setDragMode(TreeDragMode.NODE);
		stepTree.addActionHandler(this);
        stepTree.addListener((ValueChangeListener) this);
		stepTree.setItemIconPropertyId("icon");
		stepTree.setSelectable(true);
		stepTree.setImmediate(true);
        stepTree.setItemDescriptionGenerator(new PropertiesDescriptionGenerator());
        stepTree.expandItemsRecursively(rootItem);

        stepTreeHintLabel = new Label(Messages.getString("stepTree.hint"));
        
		availableTree.setDropHandler(new TreeDeleteHandler(this, stepTree));
		stepTree.setDropHandler(new TreeDropHandler(stepTree, availableTree));

		description = new Label(Messages.getString("availableTree.description.prompt"));
        description.setContentMode(Label.CONTENT_XHTML);
		description.setSizeFull();

        removeFromStepTreeButton.setEnabled(stepTree.getValue() != null);
        paramPanel = new WidgetFormWindow();

		VerticalLayout availableWidgetsLayout = new VerticalLayout();
        availableWidgetsLayout.setSpacing(true);
        availableWidgetsLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        availableWidgetsLayout.addComponent(availableTree);
        availableWidgetsLayout.addComponent(description);
        availableWidgetsLayout.addComponent(availableTreeHintLabel);

		VerticalLayout attributeLayout = new VerticalLayout();
        attributeLayout.setSizeUndefined();
		attributeLayout.setWidth(245, Sizeable.UNITS_PIXELS);
        attributeLayout.addComponent(assigneeField);
        attributeLayout.addComponent(candidateGroupsField);
        attributeLayout.addComponent(swimlaneField);

        VerticalLayout stepLayout = new VerticalLayout();
        stepLayout.setWidth(245, Sizeable.UNITS_PIXELS);
        stepLayout.addComponent(stepTree);
        stepLayout.addComponent(removeFromStepTreeButton);
        stepLayout.addComponent(stepTreeHintLabel);

        GridLayout mainLayout = new GridLayout(6, 3);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.addComponent(saveButton, 0, 0);
        mainLayout.addComponent(attributeLayout, 0, 1, 1, 1);
        mainLayout.addComponent(stepLayout, 2, 1, 3, 1);
        mainLayout.addComponent(availableWidgetsLayout, 4, 1, 5, 2);
        mainLayout.addComponent(paramPanel, 0, 2, 3, 2);

        mainLayout.setComponentAlignment(availableWidgetsLayout, Alignment.TOP_LEFT);
        mainLayout.setComponentAlignment(stepLayout, Alignment.TOP_LEFT);
        mainLayout.setComponentAlignment(paramPanel, Alignment.TOP_LEFT);

        mainLayout.setColumnExpandRatio(0, 0);
        mainLayout.setColumnExpandRatio(1, 0);
        mainLayout.setColumnExpandRatio(2, 0);
        mainLayout.setColumnExpandRatio(3, 0);
        mainLayout.setColumnExpandRatio(4, 0);
        mainLayout.setColumnExpandRatio(5, 1);
        mainLayout.setRowExpandRatio(0, 0);
        mainLayout.setRowExpandRatio(1, 0);
        mainLayout.setRowExpandRatio(2, 1);

        return mainLayout;
	}

	public void deleteTreeItem(final Object widget) {
		ConfirmDialog.show(application.getMainWindow(),
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
                            removeFromStepTreeButton.setEnabled(false);
                        } else {

                        }
                    }
                }
        );
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
            logger.log(Level.SEVERE, "Error loading available widgets", e);
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

			assigneeField.setValue(map.get(JSONHandler.ASSIGNEE));
			swimlaneField.setValue(map.get(JSONHandler.SWIMLANE));
			candidateGroupsField.setValue(map.get(JSONHandler.CANDIDATE_GROUPS));
			
			for (Object widget : stepTreeContainer.getItemIds()) {
				if (widget != rootItem)
					stepTree.getItem(widget).getItemProperty("icon").setValue(getWidgetIcon(((WidgetItemInStep) widget).getWidgetItem()));
			}
			//jsonConfig = dumpTreeToJSON();

		} catch (WidgetNotFoundException e) {
			logger.log(Level.SEVERE, "Widget not found", e);
			application.getMainWindow().showNotification(Messages.getString("error.config_not_loaded.title"),
												Messages.getString("error.config_not_loaded.widget_not_found.body", e.getWidgetItemName()),
												Notification.TYPE_ERROR_MESSAGE);
		} catch (ParsingFailedException e) {
            logger.log(Level.SEVERE, "Parsing failed found", e);
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
		} else if (event.getComponent() == removeFromStepTreeButton) {
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
		return JSONHandler.dumpTreeToJSON(stepTree, rootItem, assigneeField.getValue(), candidateGroupsField.getValue(), swimlaneField.getValue(), stepType);
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
		
		removeFromStepTreeButton.setEnabled(stepTree.getValue() != null && stepTree.getValue() != rootItem);
		
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
