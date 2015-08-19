package pl.net.bluesoft.rnd.processtool.plugins;

import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetScriptProvider;
import pl.net.bluesoft.rnd.processtool.web.view.AbstractTaskListView;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 15:56
 */
public interface GuiRegistry {
    public static final String STANDARD_PROCESS_QUEUE_ID = "standard-queue-view";

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

    Collection<GenericPortletViewRenderer> getGenericPortletViews(String portletKey);
    void registerGenericPortletViewRenderer(String portletKey, GenericPortletViewRenderer renderer);
    void unregisterGenericPortletViewRenderer(String portletKey, GenericPortletViewRenderer renderer);

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
    AbstractTaskListView getTasksListView(String viewName);

    /** register new task view */
    void registerTasksListView(String viewName, AbstractTaskListView taskListView);

    /** Unregister plugin task view */
    void unregisterTasksListView(String viewName);

	/** Get Scripts */
	String getJavaScripts();

	void registerButtonGenerator(ButtonGenerator buttonGenerator);
	void unregisterButtonGenerator(ButtonGenerator buttonGenerator);
	Collection<ButtonGenerator> getButtonGenerators();

    /* Get all queues avaiable to user with given login */
    List<AbstractTaskListView> getTasksListViews(String currentUserLogin);

	void registerTaskPermissionChecker(TaskPermissionChecker permissionChecker);
	void unregisterTaskPermissionChecker(TaskPermissionChecker permissionChecker);
	List<TaskPermissionChecker> getTaskPermissionCheckers();

	void registerActionPermissionChecker(ActionPermissionChecker permissionChecker);
	void unregisterActionPermissionChecker(ActionPermissionChecker permissionChecker);
	List<ActionPermissionChecker> getActionPermissionCheckers();
}
