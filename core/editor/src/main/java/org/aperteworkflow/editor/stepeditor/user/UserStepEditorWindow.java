package org.aperteworkflow.editor.stepeditor.user;


import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Tree.TreeDragMode;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.Reindeer;
import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.stepeditor.AbstractStepEditorWindow;
import org.aperteworkflow.editor.stepeditor.StepEditorApplication;
import org.aperteworkflow.editor.stepeditor.user.JSONHandler.ParsingFailedException;
import org.aperteworkflow.editor.stepeditor.user.JSONHandler.WidgetNotFoundException;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import org.aperteworkflow.editor.ui.permission.PermissionEditor;
import org.aperteworkflow.editor.ui.permission.PermissionProvider;
import org.aperteworkflow.editor.ui.property.PropertiesForm;
import org.aperteworkflow.editor.ui.property.PropertiesPanel;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import org.vaadin.dialogs.ConfirmDialog;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aperteworkflow.util.vaadin.VaadinUtility.htmlLabel;
import static org.aperteworkflow.util.vaadin.VaadinUtility.styled;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

public class UserStepEditorWindow extends AbstractStepEditorWindow implements Handler, ValueChangeListener {

	private static final Logger logger = Logger.getLogger(UserStepEditorWindow.class.getName());
    private static final Action[] COMMON_ACTIONS = new Action[] {};

	private HierarchicalContainer stepTreeContainer;
	private Tree stepTree;
	private Component availableWidgetsPane;
	private WidgetItemInStep rootItem;
	private TextField assigneeField;
	private TextField candidateGroupsField;
	private TextField swimlaneField;
    private TextField descriptionField;
    private RichTextArea commentaryTextArea;
	private WidgetFormWindow paramPanel;
    private PermissionEditor permissionEditor;

    private Action actionDelete;
	private Action[] actions;
    private Collection<Permission> permissions;

    public UserStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
        permissions = new LinkedHashSet<Permission>();
        actionDelete = new Action(I18NSource.ThreadUtil.getThreadI18nSource().getMessage("stepTree.action.delete"));
		actions = new Action[] { actionDelete };
    }

    @Override
	public ComponentContainer init() {
		ComponentContainer comp = buildLayout();
		
		if (jsonConfig != null && jsonConfig.trim().length() > 0) {
			if (stepTreeContainer != null && rootItem != null) {
				loadJSONConfig();
			}
		}

        permissionEditor.loadData();
		
		return comp;
	}
	
	private ComponentContainer buildLayout() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource(); 
        prepareAvailableWidgetsComponent();
        
		stepTree = new Tree(messages.getMessage("stepTree.title"), getCurrentStep());
        stepTree.setItemCaptionMode(Tree.ITEM_CAPTION_MODE_PROPERTY);
        stepTree.setItemCaptionPropertyId("name");
		stepTree.setDragMode(TreeDragMode.NODE);
		stepTree.addActionHandler(this);
        stepTree.addListener(this);
		stepTree.setSelectable(true);
		stepTree.setImmediate(true);
        stepTree.setItemDescriptionGenerator(new PropertiesDescriptionGenerator());
        stepTree.expandItemsRecursively(rootItem);
		stepTree.setDropHandler(new TreeDropHandler(stepTree, stepTreeContainer));

        paramPanel = new WidgetFormWindow();

        permissionEditor = new PermissionEditor();
        permissionEditor.setMargin(true);
        permissionEditor.setProvider(new PermissionProvider() {
            @Override
            public Collection<Permission> getPermissions() {
                return permissions;
            }

            @Override
            public Collection<PermissionDefinition> getPermissionDefinitions() {
                PermissionDefinition perm1 = new PermissionDefinition();
                perm1.setKey("SEARCH");
                perm1.setDescription("editor.permissions.description.step.SEARCH");
                return Arrays.asList(perm1);
            }

            @Override
            public boolean isNewPermissionDefinitionAllowed() {
                return false;
            }

            @Override
            public void addPermission(Permission permission) {
                permissions.add(permission);
            }

            @Override
            public void removePermission(Permission permission) {
                permissions.remove(permission);
            }
        });

        VerticalLayout assignmentLayout = prepareAssignmentLayout();
        VerticalLayout stepDefinitionLayout = buildStateDefinitionLayout();
        VerticalLayout stepLayout = buildWidgetEditorTabContent();

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.setSpacing(true);
        vl.addComponent(new Label(messages.getMessage("userstep.editor.instructions"), Label.CONTENT_XHTML));

        TabSheet ts = new TabSheet();
        ts.setSizeFull();
        ts.addTab(stepLayout, messages.getMessage("userstep.editor.widgets.tabcaption"));
        ts.addTab(stepDefinitionLayout, messages.getMessage("userstep.state.tabcaption"));
        ts.addTab(assignmentLayout, messages.getMessage("userstep.editor.assignment.tabcaption"));
        ts.addTab(permissionEditor, messages.getMessage("userstep.editor.permissions.tabcaption"));
        ts.setSelectedTab(stepLayout);
        vl.addComponent(ts);
        vl.setExpandRatio(ts, 1.0f);
        return vl;

	}

    private VerticalLayout prepareAssignmentLayout() {
    	I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
    	
    	assigneeField = new TextField();//Messages.getString("field.assignee"));
        assigneeField.setWidth("100%");
        assigneeField.setNullRepresentation("");
        
        candidateGroupsField = new TextField();//Messages.getString("field.candidateGroups"));
        candidateGroupsField.setWidth("100%");
        candidateGroupsField .setNullRepresentation("");
        
        swimlaneField = new TextField();//Messages.getString("field.swimlane"));
        swimlaneField.setWidth("100%");
        swimlaneField.setNullRepresentation("");
        
        VerticalLayout assignmentLayout = new VerticalLayout();
        assignmentLayout.setWidth("100%");
        assignmentLayout.setSpacing(true);
        assignmentLayout.setMargin(true);
        assignmentLayout.addComponent(styled(new Label(messages.getMessage("field.assignee")), "h1"));
        assignmentLayout.addComponent(htmlLabel(messages.getMessage("field.assignee.info")));
        assignmentLayout.addComponent(assigneeField);
        assignmentLayout.addComponent(styled(new Label(messages.getMessage("field.candidateGroups")), "h1"));
        assignmentLayout.addComponent(htmlLabel(messages.getMessage("field.candidateGroups.info")));
        assignmentLayout.addComponent(candidateGroupsField);
        assignmentLayout.addComponent(styled(new Label(messages.getMessage("field.swimlane")), "h1"));
        assignmentLayout.addComponent(htmlLabel(messages.getMessage("field.swimlane.info")));
        assignmentLayout.addComponent(swimlaneField);
        return assignmentLayout;
    }

    private void prepareAvailableWidgetsComponent() {
        List<WidgetItem> availableWidgetItems = getAvailableWidgetItems();
        Collections.sort(availableWidgetItems, new Comparator<WidgetItem>(){
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
            availableWidgets = WidgetInfoLoader.loadAvailableWidgets(application);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading available widgets", e);
        }
        for (Entry<BundleItem, Collection<WidgetItem>> entry : availableWidgets.entrySet()) {
            final Collection<WidgetItem> widgets = entry.getValue();
            for (WidgetItem widgetItem : widgets) {
                widgetItems.add(widgetItem);
            }
        }
        return widgetItems;
    }

    private VerticalLayout buildWidgetEditorTabContent() {
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

    private VerticalLayout buildStateDefinitionLayout() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        descriptionField = new TextField();
        descriptionField.setNullRepresentation("");
        descriptionField.setWidth("100%");
        
        commentaryTextArea = new RichTextArea();
        commentaryTextArea.setNullRepresentation("");
        commentaryTextArea.setWidth("100%");

        VerticalLayout stateLayout = new VerticalLayout();
        stateLayout.setWidth("100%");
        stateLayout.setSpacing(true);
        stateLayout.setMargin(true);

        stateLayout.addComponent(styled(new Label(messages.getMessage("field.description")), "h1"));
        stateLayout.addComponent(htmlLabel(messages.getMessage("field.description.info")));
        stateLayout.addComponent(descriptionField);

        stateLayout.addComponent(styled(new Label(messages.getMessage("field.commentary")), "h1"));
        stateLayout.addComponent(htmlLabel(messages.getMessage("field.commentary.info")));
        stateLayout.addComponent(commentaryTextArea);

        return stateLayout;
    }

    public void deleteTreeItem(final Object widget) {
    	I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
    	ConfirmDialog.show(application.getMainWindow(),
    			messages.getMessage("dialog.delete.title"),
    			messages.getMessage("dialog.delete.question"),
    			messages.getMessage("dialog.delete.confirm"),
    			messages.getMessage("dialog.delete.cancel"),
                new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            HierarchicalContainer hc = (HierarchicalContainer) stepTree.getContainerDataSource();
                            hc.removeItemRecursively(widget);
                            showParams(null);
                        }
                    }
                }
        );
	}

	@Override
	public Action[] getActions(Object target, Object sender) {
		if (target != rootItem) {
			return actions;
        } else {
			return COMMON_ACTIONS;
        }
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
		final String path = messages.getMessage(pathKey);
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

	@Override
	public void handleAction(Action action, Object sender, Object target) {
		if (action == actionDelete) {
			deleteTreeItem(target);
		}
	}

	private void loadJSONConfig() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		
		try {
			Map<String, String> map = JSONHandler.loadConfig(stepTreeContainer, rootItem, jsonConfig, permissions);
			if (stepTree != null) {
				stepTree.expandItemsRecursively(rootItem);
			}

			assigneeField.setValue(map.get(JSONHandler.ASSIGNEE));
			swimlaneField.setValue(map.get(JSONHandler.SWIMLANE));
			candidateGroupsField.setValue(map.get(JSONHandler.CANDIDATE_GROUPS));
            commentaryTextArea.setValue(map.get(JSONHandler.COMMENTARY));
            descriptionField.setValue(map.get(JSONHandler.DESCRIPTION));

			for (Object widget : stepTreeContainer.getItemIds()) {
				if (widget != rootItem) {
					stepTree.getItem(widget).getItemProperty("icon").setValue(getWidgetIcon(((WidgetItemInStep) widget).getWidgetItem()));
                }
            }
		} catch (WidgetNotFoundException e) {
			logger.log(Level.SEVERE, "Widget not found", e);
			application.getMainWindow().showNotification(messages.getMessage("error.config_not_loaded.title"),
					messages.getMessage("error.config_not_loaded.widget_not_found.body", e.getWidgetItemName()),
												Notification.TYPE_ERROR_MESSAGE);
		} catch (ParsingFailedException e) {
            logger.log(Level.SEVERE, "Parsing failed found", e);
			application.getMainWindow().showNotification(	messages.getMessage("error.config_not_loaded.title"),
					messages.getMessage("error.config_not_loaded.unexpected_error.body", e.getLocalizedMessage()),
												Notification.TYPE_ERROR_MESSAGE);
		}
	}

	private HierarchicalContainer prepareTreeContainer() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		stepTreeContainer = new HierarchicalContainer();
		stepTreeContainer.addContainerProperty("name", String.class, messages.getMessage("stepTree.name.default"));
		stepTreeContainer.addContainerProperty("icon", Resource.class, getResource("icon.widget.default"));

		final WidgetItem widgetItem = new WidgetItem(
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

	private void showParams(WidgetItemInStep widget) {
        paramPanel.loadWidget(widget, true);
	}

    @Override
	public void save() {
        // perform validation of all the widget definitions
        Collection<?> itemIds = stepTreeContainer.getItemIds();
        for (Object itemId : itemIds) {
            WidgetItemInStep widgetInStep = (WidgetItemInStep) itemId;

            PropertiesPanel propertiesPanel = widgetInStep.getWidgetPropertiesPanel();
            if (propertiesPanel != null) {
                widgetInStep.getWidgetPropertiesPanel().ensureForm();

                PropertiesForm propertiesForm = propertiesPanel.getPropertiesForm();
                if (!propertiesForm.isValid()) {
                    I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
                    VaadinUtility.validationNotification(
                            application,
                            messages,
                            messages.getMessage("stepTree.contains.invalid")
                    );

                    // switch to problematic widget
                    paramPanel.loadWidget(widgetInStep, false);
                    stepTree.setValue(widgetInStep);
                    return;
                }
            }
        }

        String json = JSONHandler.dumpTreeToJSON(stepTree, rootItem,
                assigneeField.getValue(), candidateGroupsField.getValue(), swimlaneField.getValue(),
                stepType, descriptionField.getValue(), commentaryTextArea.getValue(),
                permissions
        );
        application.getJsHelper().postAndRedirectStep(url, json);
	}

    @Override
    public void valueChange(ValueChangeEvent event) {
		if (event.getProperty() == stepTree) {
			if (stepTree.getValue() == null || stepTree.getValue() == rootItem) {
			    showParams(null);
			} else {
			    WidgetItemInStep widget = (WidgetItemInStep) stepTree.getValue();
			    stepTree.setValue(widget);
			    showParams(widget);
			}
		}
	}

}
