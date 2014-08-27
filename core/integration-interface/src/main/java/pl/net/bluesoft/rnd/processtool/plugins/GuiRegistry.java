package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetScriptProvider;
import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBeanFactory;

import java.util.Collection;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 15:56
 */
public interface GuiRegistry {
	void registerWidget(Class<? extends ProcessToolWidget> clazz);
	void unregisterWidget(Class<? extends ProcessToolWidget> clazz);
	Map<String, Class<? extends ProcessToolWidget>> getAvailableWidgets();
	ProcessToolWidget createWidget(String widgetName);

	void registerButton(Class<? extends ProcessToolActionButton> clazz);
	void unregisterButton(Class<? extends ProcessToolActionButton> clazz);
	Map<String,Class<? extends ProcessToolActionButton>> getAvailableButtons();
	ProcessToolActionButton createButton(String buttonName);

	void registerStep(Class<? extends ProcessToolProcessStep> clazz);
	void unregisterStep(Class<? extends ProcessToolProcessStep> clazz);
	Map<String, Class<? extends ProcessToolProcessStep>> getAvailableSteps();
	ProcessToolProcessStep createStep(String stepName);

	/** Register new javaScript file for html widgets */
	void registerJavaScript(String fileName, IWidgetScriptProvider scriptProvider);

	/** Unregister new javaScript file for html widgets */
	void unregisterJavaScript(String fileName);

	/** Register new html view for widgets */
	void registerHtmlView(String widgetName, ProcessHtmlWidget scriptProvider);

	/** Unregister new html view for widgets */
	void unregisterHtmlView(String widgetName);

	/** Get Html Widget definition */
	ProcessHtmlWidget getHtmlWidget(String widgetName);

	Collection<ProcessHtmlWidget> getHtmlWidgets();

	/** Get plugin controller for web invocation */
	IOsgiWebController getWebController(String controllerName);

	/** register new plugin contorller */
	void registerWebController(String controllerName, IOsgiWebController controller);

	/** Unregister plugin web controller */
	void unregisterWebController(String controllerName);

    /** Get plugin task view */
    TasksListViewBeanFactory getTasksListView(String viewName);

    /** register new task view */
    void registerTasksListView(String viewName, TasksListViewBeanFactory view);

    /** Unregister plugin task view */
    void unregisterTasksListView(String viewName);

	/** Get Scripts */
	String getJavaScripts();

	void registerButtonGenerator(ButtonGenerator buttonGenerator);
	void unregisterButtonGenerator(ButtonGenerator buttonGenerator);
	Collection<ButtonGenerator> getButtonGenerators();
}
