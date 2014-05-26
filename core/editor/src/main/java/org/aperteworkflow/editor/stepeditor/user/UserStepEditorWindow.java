package org.aperteworkflow.editor.stepeditor.user;


import com.vaadin.ui.*;
import com.vaadin.ui.Window.Notification;
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
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.singletonList;
import static org.aperteworkflow.util.vaadin.VaadinUtility.htmlLabel;
import static org.aperteworkflow.util.vaadin.VaadinUtility.styled;
import static pl.net.bluesoft.util.lang.Strings.hasText;

public class UserStepEditorWindow extends AbstractStepEditorWindow  {
	private static final Logger logger = Logger.getLogger(UserStepEditorWindow.class.getName());

	private TextField assigneeField;
	private TextField candidateGroupsField;
	private TextField swimlaneField;
    private TextField descriptionField;
    private RichTextArea commentaryTextArea;
	private TextField stepInfoField;
    private PermissionEditor permissionEditor;

    private Collection<Permission> permissions;

	private WidgetEditor widgetEditor;

    public UserStepEditorWindow(StepEditorApplication application, String jsonConfig, String url, String stepName, String stepType) {
		super(application, jsonConfig, url, stepName, stepType);
        permissions = new LinkedHashSet<Permission>();
		widgetEditor = new WidgetEditor(application);
    }

    @Override
	public ComponentContainer init() {
		ComponentContainer comp = buildLayout();
		
		if (hasText(jsonConfig) && widgetEditor.isInitialized()) {
			loadJSONConfig();
		}

        permissionEditor.loadData();
		
		return comp;
	}

	private ComponentContainer buildLayout() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		widgetEditor.init();

		initPermissionEditor();

        VerticalLayout assignmentLayout = prepareAssignmentLayout();
        VerticalLayout stepDefinitionLayout = buildStateDefinitionLayout();
        VerticalLayout stepLayout = widgetEditor.buildWidgetEditorTabContent();

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

	private void initPermissionEditor() {
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
				return singletonList(perm1);
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

    private VerticalLayout buildStateDefinitionLayout() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        descriptionField = new TextField();
        descriptionField.setNullRepresentation("");
        descriptionField.setWidth("100%");
        
        commentaryTextArea = new RichTextArea();
        commentaryTextArea.setNullRepresentation("");
        commentaryTextArea.setWidth("100%");

		stepInfoField = new TextField();
		stepInfoField.setNullRepresentation("");
		stepInfoField.setWidth("100%");

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

		stateLayout.addComponent(styled(new Label(messages.getMessage("field.stepInfo")), "h1"));
		stateLayout.addComponent(htmlLabel(messages.getMessage("field.stepInfo.info")));
		stateLayout.addComponent(stepInfoField);

        return stateLayout;
    }

	private void loadJSONConfig() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		
		try {
			Map<String, String> map = JSONHandler.loadConfig(widgetEditor.getStepTreeContainer(), widgetEditor.getRootItem(), jsonConfig, permissions);

			assigneeField.setValue(map.get(JSONHandler.ASSIGNEE));
			swimlaneField.setValue(map.get(JSONHandler.SWIMLANE));
			candidateGroupsField.setValue(map.get(JSONHandler.CANDIDATE_GROUPS));
            descriptionField.setValue(map.get(JSONHandler.DESCRIPTION));
			commentaryTextArea.setValue(map.get(JSONHandler.COMMENTARY));
			stepInfoField.setValue(map.get(JSONHandler.STEP_INFO));

			widgetEditor.expandRecursively();
			widgetEditor.loadJSONConfig();
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

	@Override
	public void save() {
        // perform validation of all the widget definitions
        Collection<?> itemIds = widgetEditor.getStepTreeContainer().getItemIds();
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

					widgetEditor.switchToProblematicWidget(widgetInStep);
					return;
                }
            }
        }

        String json = JSONHandler.dumpTreeToJSON(widgetEditor.getStepTree(), widgetEditor.getRootItem(),
                assigneeField.getValue(), candidateGroupsField.getValue(), swimlaneField.getValue(),
                stepType, descriptionField.getValue(), commentaryTextArea.getValue(),
				stepInfoField.getValue(), permissions
        );
        application.getJsHelper().postAndRedirectStep(url, json);
	}
}
