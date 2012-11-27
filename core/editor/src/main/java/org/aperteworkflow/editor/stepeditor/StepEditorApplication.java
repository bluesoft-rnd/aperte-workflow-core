package org.aperteworkflow.editor.stepeditor;


import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.*;
import org.aperteworkflow.editor.stepeditor.auto.AutoStepEditorWindow;
import org.aperteworkflow.editor.stepeditor.user.UserStepEditorWindow;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    	I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
    	Label headerLabel = new Label();
        if (stepName != null && !stepName.isEmpty()) {
            headerLabel.setValue(messages.getMessage("userStep.stepName", new Object[] { stepName }));
        } else {
            headerLabel.setValue(messages.getMessage("userStep.noStepName"));
        }
        headerLabel.addStyleName("h1");

        return headerLabel;
    }

    private ComponentContainer buildHeader(final AbstractStepEditorWindow sew, String stepType) {
		Component label = getHeaderLabel();
        Select stepList = prepareStepList(stepType);
        Button saveButton = new Button(I18NSource.ThreadUtil.getThreadI18nSource().getMessage("jse.button.save"), new Button.ClickListener() {
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
        
        // method-level class used for sorting
        class Item implements Comparable<Item> {
        	public String name;
        	public String caption;
        	
			public Item(String name, String caption) {
				this.name = name;
				this.caption = caption;
			}
			
			@Override
			public int compareTo(Item o) {
				return caption.compareTo(o.caption);
			}
        }
        
        List<Item> items = new LinkedList<Item>();
        
        // add User tasktype
        items.add(new Item("User","User"));
       
        // other tasks
        Map<String,ProcessToolProcessStep> availableSteps = getRegistry().getAvailableSteps();
        for (ProcessToolProcessStep stepInstance : availableSteps.values()) {
            Class stepClass = stepInstance.getClass();
            AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
            items.add(new Item(a.name(),a.name()));
        }
        
        Collections.sort(items);
        
        for (Item item:items){
        	stepList.addItem(item.name);
        	stepList.setItemCaption(item.name, item.caption);
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
        
		mainWindow = new Window(I18NSource.ThreadUtil.getThreadI18nSource().getMessage("application.title"));
		mainWindow.addParameterHandler(this);

        jsHelper = new JavaScriptHelper(mainWindow);
        jsHelper.preventWindowClosing();

		setMainWindow(mainWindow);
	}

}
