package pl.net.bluesoft.rnd.pt.ext.stepeditor;


import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.*;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.processtool.plugins.PluginMetadata;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.auto.AutoStepEditorWindow;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.user.UserStepEditorWindow;
import pl.net.bluesoft.util.lang.Classes;

import javax.servlet.ServletContext;
import java.util.Collection;
import java.util.Map;

public class StepEditorApplication extends Application implements ParameterHandler {

	private static final long		serialVersionUID	= 2136349026207825108L;
	private static final String     TASKTYPE_USER       = "User";

	private Window					mainWindow;
	private JavaScriptHelper		jsHelper;
    private String					url;

	public Window getMainWindow() {
		return mainWindow;
	}
	
	public JavaScriptHelper getJsHelper() {
		return jsHelper;
	}

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		String stepName = null, jsonConfig = null;
		
		String[] stepNames = parameters.get("stepname");
		if (stepNames != null && stepNames.length > 0 && !StringUtils.isEmpty(stepNames[0])) {
			stepName = stepNames[0];
		}
		
		String[] stepConfig = parameters.get("step_config");
		if (stepConfig != null && stepConfig.length > 0 && !StringUtils.isEmpty(stepConfig[0])) {
			jsonConfig = stepConfig[0];
		}
		
		String[] urls = parameters.get("callback_url");
		if (urls != null && urls.length > 0 && !StringUtils.isEmpty(urls[0])) {
			url = urls[0];
		}
		
		refresh(stepName, jsonConfig);
		
	}

	private void refresh(String stepName, String jsonConfig) {
		AbstractStepEditorWindow stepEditorWindow;
		if (TASKTYPE_USER.equals(stepName)) {
			stepEditorWindow = new UserStepEditorWindow(this,jsonConfig,url,stepName);
		} else {
			stepEditorWindow = new AutoStepEditorWindow(this,jsonConfig,url,stepName);
		}
		
		ComponentContainer window = stepEditorWindow.init();
		ComponentContainer header = buildHeader(stepEditorWindow, stepName);
		refreshWindow(header, window);
	}
	
	private ComponentContainer buildHeader(AbstractStepEditorWindow sew, String stepName) {
		Label title = sew.getHeaderLabel();
		
		VerticalLayout header = new VerticalLayout();
		header.setSpacing(true);
		header.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		header.addComponent(title);
		header.setExpandRatio(title, 0);
		Select stepList = prepareStepList(stepName);
		header.addComponent(stepList);
		header.setExpandRatio(stepList, 0);
		
		return header;
	}
	
	private void refreshWindow(ComponentContainer header, ComponentContainer windowContainer) {
		mainWindow.removeAllComponents();
		VerticalLayout main = new VerticalLayout();
		main.addComponent(header);
		main.addComponent(windowContainer);
		mainWindow.setContent(main);
	}
	
	private Select prepareStepList(String stepName) {
		final Select stepList = new Select();
		stepList.setNullSelectionAllowed(false);
        stepList.setImmediate(true);
        
        //add User tasktype
        stepList.addItem("User");
        stepList.setItemCaption("User", "User");
        
		ProcessToolRegistry reg = getRegistry(this);

        Collection<PluginMetadata> metadata = reg.getPluginManager().getRegisteredPlugins();
        for (PluginMetadata bm : metadata) {
            for (Class<?> step : bm.getStepClasses()) {
                AliasName a = Classes.getClassAnnotation(step, AliasName.class);
                stepList.addItem(a.name());
                stepList.setItemCaption(a.name(), a.name());
            }
        }
		
		stepList.setValue(stepName);
		
		stepList.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
            	String sn = (String)stepList.getValue();
            	refresh(sn, null);
            }
        });
		
		return stepList;
	}
	
	@Override
	public void init() {

		mainWindow = new Window(Messages.getString("application.title"));
		jsHelper = new JavaScriptHelper(mainWindow);
		jsHelper.preventWindowClosing();
		mainWindow.addParameterHandler(this);
		setMainWindow(mainWindow);
	}

	
	public static ProcessToolRegistry getRegistry(Application application) {
		ApplicationContext ctx = application.getContext();
		WebApplicationContext webCtx = (WebApplicationContext) ctx;
		ServletContext sc = webCtx.getHttpSession().getServletContext();
		return (ProcessToolRegistry) sc.getAttribute(ProcessToolRegistry.class.getName());
	}	
}
