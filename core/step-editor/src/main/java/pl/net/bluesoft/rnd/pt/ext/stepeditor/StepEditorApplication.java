package pl.net.bluesoft.rnd.pt.ext.stepeditor;


import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.auto.AutoStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.UserStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.util.lang.Classes;

import java.util.Map;

public class StepEditorApplication extends GenericEditorApplication implements ParameterHandler {

	private static final long		serialVersionUID	= 2136349026207825108L;
    private static final String     TASKTYPE_USER       = "User";

	private Window					mainWindow;
	private JavaScriptHelper		jsHelper;
    private String					url;
    private String                  stepName;
    
    

	public Window getMainWindow() {
		return mainWindow;
	}
	
	public JavaScriptHelper getJsHelper() {
		return jsHelper;
	}

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
        if (parameters == null || parameters.size() == 0) {
            // No parameters to handle, we are not interested in such a request
            // it may be a request for static resource e.g. <servlet>/APP/323/root.gif
            return;
        }

		String stepType = getStringParameterByName("stepType", parameters);
        if (stepType == null) {
            // No stepType in request, we have nothing to refresh
            return;
        }

        String jsonConfig = getStringParameterByName("stepConfig", parameters);
        stepName = getStringParameterByName("stepName", parameters);
        url = getStringParameterByName("callbackUrl", parameters);
		
		refresh(stepName, stepType, jsonConfig);
	}

    private void refresh(String stepName, String stepType, String jsonConfig) {
		AbstractStepEditorWindow stepEditorWindow;
		if (TASKTYPE_USER.equals(stepType)) {
			stepEditorWindow = new UserStepEditorWindow(this, jsonConfig, url, stepName, stepType);
		} else {
			stepEditorWindow = new AutoStepEditorWindow(this, jsonConfig, url, stepName, stepType);
		}
		
		ComponentContainer window = stepEditorWindow.init();
		ComponentContainer header = buildHeader(stepEditorWindow, stepType);
		refreshWindow(header, window);
	}

    public Label getHeaderLabel() {
        Label headerLabel = new Label();
        if (stepName != null && !stepName.isEmpty()) {
            headerLabel.setValue(Messages.getString("userStep.stepName", stepName));
        } else {
            headerLabel.setValue(Messages.getString("userStep.noStepName"));
        }
        headerLabel.addStyleName("h1");

        return headerLabel;
    }

    private ComponentContainer buildHeader(final AbstractStepEditorWindow sew, String stepType) {
		Component label = getHeaderLabel();
        Select stepList = prepareStepList(stepType);
        Button saveButton = new Button(Messages.getString("jse.button.save"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
               sew.save();
            }
        });
        saveButton.setClickShortcut(ShortcutAction.KeyCode.S,
                        ShortcutAction.ModifierKey.CTRL
                );
        saveButton.setDescription("Ctrl-S");
		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setSpacing(true);
        headerLayout.setWidth("100%");
        headerLayout.addComponent(label);
        headerLayout.addComponent(saveButton);
		headerLayout.addComponent(stepList);
        headerLayout.setExpandRatio(label, 1.0f);
        headerLayout.setComponentAlignment(saveButton, Alignment.TOP_RIGHT);
        headerLayout.setComponentAlignment(stepList, Alignment.TOP_RIGHT);

		return headerLayout;
	}
	
	private void refreshWindow(ComponentContainer header, ComponentContainer windowContainer) {
		mainWindow.removeAllComponents();
		VerticalLayout main = new VerticalLayout();
        main.setMargin(true);
        main.setSpacing(true);
		main.addComponent(header);
		main.addComponent(windowContainer);
		mainWindow.setContent(main);
	}
	
	private Select prepareStepList(String stepType) {
		final Select stepList = new Select();
		stepList.setNullSelectionAllowed(false);
        stepList.setImmediate(true);
        stepList.setWidth("250px");
        //add User tasktype
        stepList.addItem("User");
        stepList.setItemCaption("User", "User");
        
		ProcessToolRegistry reg = getRegistry();

        Map<String,ProcessToolProcessStep> availableSteps = reg.getAvailableSteps();
        for (ProcessToolProcessStep stepInstance : availableSteps.values()) {
            Class stepClass = stepInstance.getClass();
            AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
            stepList.addItem(a.name());
            stepList.setItemCaption(a.name(), a.name());
        }
		stepList.setValue(stepType);
		
		stepList.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
            	String sn = (String) stepList.getValue();
            	refresh(stepName, sn, null);
            }
        });
		
		return stepList;
	}
	
	@Override
	public void init() {
        super.init();
        
		mainWindow = new Window(Messages.getString("application.title"));
		mainWindow.addParameterHandler(this);

        jsHelper = new JavaScriptHelper(mainWindow);
        jsHelper.preventWindowClosing();

		setMainWindow(mainWindow);
	}

}
