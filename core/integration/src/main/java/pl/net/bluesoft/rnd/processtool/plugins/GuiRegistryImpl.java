package pl.net.bluesoft.rnd.processtool.plugins;

import com.google.common.io.CharStreams;
import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.domain.IHtmlTemplateProvider;
import pl.net.bluesoft.rnd.processtool.web.domain.IWidgetScriptProvider;
import pl.net.bluesoft.rnd.processtool.web.view.AbstractTaskListView;
import pl.net.bluesoft.util.lang.Classes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static pl.net.bluesoft.rnd.util.AnnotationUtil.getAliasName;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 15:32
 */
@Component
@Scope(value = "singleton")
public class GuiRegistryImpl implements GuiRegistry {
	private static final Logger logger = Logger.getLogger(GuiRegistryImpl.class.getSimpleName());

	private final Map<String, Class<? extends ProcessToolWidget>> widgets = new HashMap<String, Class<? extends ProcessToolWidget>>();
	private final Map<String, Class<? extends ProcessToolActionButton>> buttons = new HashMap<String, Class<? extends ProcessToolActionButton>>();
	private final Map<String, Class<? extends ProcessToolProcessStep>> steps = new HashMap<String, Class<? extends ProcessToolProcessStep>>();

	private final Map<String, ProcessHtmlWidget> htmlWidgets = new HashMap<String, ProcessHtmlWidget>();
	private final Map<String, IWidgetScriptProvider> widgetScriptProviders = new HashMap<String, IWidgetScriptProvider>();
	private final Map<String, IOsgiWebController> webControllers = new HashMap<String, IOsgiWebController>();
    private final Map<String, AbstractTaskListView> tasksListViews = new HashMap<String, AbstractTaskListView>();

	private final Set<ButtonGenerator> buttonGenerators = new LinkedHashSet<ButtonGenerator>();

	private final List<TaskPermissionChecker> taskPermissionCheckers = new ArrayList<TaskPermissionChecker>();
	private final List<ActionPermissionChecker> actionPermissionCheckers = new ArrayList<ActionPermissionChecker>();


    private final Map<String, Set<GenericPortletViewRenderer>> genericPortletViewRenderers = new HashMap<String, Set<GenericPortletViewRenderer>>();

	private String javaScriptContent = "";

	@Autowired
	private IHtmlTemplateProvider templateProvider;

    @Autowired
    private IUserSource userSource;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

	@Override
	public synchronized void registerWidget(Class<? extends ProcessToolWidget> clazz) {
		String aliasName = getAliasName(clazz);
		widgets.put(aliasName, clazz);
		logger.fine("Registered widget alias: " + aliasName + " -> " + clazz.getName());
	}

	@Override
	public synchronized void unregisterWidget(Class<? extends ProcessToolWidget> clazz) {
		String aliasName = getAliasName(clazz);
		widgets.remove(aliasName);
		logger.fine("Unregistered widget alias: " + aliasName + " -> " + clazz.getName());
	}

	@Override
	public synchronized Map<String, Class<? extends ProcessToolWidget>> getAvailableWidgets() {
		return new HashMap<String, Class<? extends ProcessToolWidget>>(widgets);
	}

	@Override
	public synchronized ProcessToolWidget createWidget(String widgetName) {
		Class<? extends ProcessToolWidget> clazz = widgets.get(widgetName);
		checkClassFound(widgetName, clazz);
		return Classes.newInstance(clazz);
	}

	@Override
	public synchronized void registerButton(Class<? extends ProcessToolActionButton> clazz) {
		String aliasName = getAliasName(clazz);
		buttons.put(aliasName, clazz);
		logger.finest("Registered button alias: " + aliasName + " -> " + clazz.getName());
	}

	@Override
	public synchronized void unregisterButton(Class<? extends ProcessToolActionButton> clazz) {
		String aliasName = getAliasName(clazz);
		buttons.remove(aliasName);
		logger.finest("Unregistered button alias: " + aliasName + " -> " + clazz.getName());
	}

	@Override
	public synchronized Map<String,Class<? extends ProcessToolActionButton>> getAvailableButtons() {
		return new HashMap<String, Class<? extends ProcessToolActionButton>>(buttons);
	}

	@Override
	public synchronized ProcessToolActionButton createButton(String buttonName) {
		Class<? extends ProcessToolActionButton> aClass = buttons.get(buttonName);
		checkClassFound(buttonName, aClass);
		return Classes.newInstance(aClass);
	}

	@Override
	public synchronized void registerStep(Class<? extends ProcessToolProcessStep> clazz) {
		String aliasName = getAliasName(clazz);
		steps.put(aliasName, clazz);
		logger.finest("Registered step extension: " + aliasName);
	}

	@Override
	public synchronized void unregisterStep(Class<? extends ProcessToolProcessStep> clazz) {
		String aliasName = getAliasName(clazz);
		steps.remove(aliasName);
		logger.finest("Unregistered step extension: " + aliasName);
	}

	@Override
	public synchronized Map<String, Class<? extends ProcessToolProcessStep>> getAvailableSteps() {
		return new HashMap<String, Class<? extends ProcessToolProcessStep>>(steps);
	}

	@Override
	public synchronized ProcessToolProcessStep createStep(String stepName) {
		Class<? extends ProcessToolProcessStep> clazz = steps.get(stepName);
		checkClassFound(stepName, clazz);
        /* Create step with spring context */
		return (ProcessToolProcessStep)beanFactory.createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
	}

	@Override
	public synchronized void registerJavaScript(String fileName,IWidgetScriptProvider scriptProvider)
	{
		widgetScriptProviders.put(fileName, scriptProvider);

		InputStream javaScript = scriptProvider.getJavaScriptContent();

		String compressedScript = compress(javaScript);
		javaScriptContent += compressedScript;
	}

	@Override
	public synchronized void unregisterJavaScript(String fileName)
	{
		widgetScriptProviders.remove(fileName);
	}

	@Override
	public synchronized void registerHtmlView(String widgetName,ProcessHtmlWidget processHtmlWidget)
	{
		htmlWidgets.put(widgetName, processHtmlWidget);

        if(!processHtmlWidget.hasContnet())
        {
            logger.log(Level.INFO, "Widget "+processHtmlWidget.getWidgetName()+" has no content");
            return;
        }

		try
		{
			InputStream htmlFileStream = processHtmlWidget.getContentProvider().getHtmlContent();
			String htmlBody = CharStreams.toString(new InputStreamReader(htmlFileStream, "UTF-8"));

			templateProvider.addTemplate(widgetName, htmlBody);
		}
		catch(Exception ex)
		{
			throw new RuntimeException("Problem during adding new html template", ex);
		}
	}

	@Override
	public synchronized void unregisterHtmlView(String widgetName)
	{
		htmlWidgets.remove(widgetName);

		templateProvider.removeTemplate(widgetName);
	}

	@Override
	public synchronized ProcessHtmlWidget getHtmlWidget(String widgetName) {
		return htmlWidgets.get(widgetName);
	}

	@Override
	public synchronized Collection<ProcessHtmlWidget> getHtmlWidgets() {
		return new ArrayList<ProcessHtmlWidget>(htmlWidgets.values());
	}

	@Override
	public IOsgiWebController getWebController(String controllerName) {
		return webControllers.get(controllerName);
	}

	@Override
	public void registerWebController(String controllerName, IOsgiWebController controller) {
		webControllers.put(controllerName, controller);
	}

	@Override
	public void unregisterWebController(String controllerName) {
		webControllers.remove(controllerName);
	}


	@Override
	public synchronized String getJavaScripts()
	{
		return decompress(javaScriptContent);
	}

	@Override
	public void registerButtonGenerator(ButtonGenerator buttonGenerator) {
		buttonGenerators.add(buttonGenerator);
	}

	@Override
	public void unregisterButtonGenerator(ButtonGenerator buttonGenerator) {
		buttonGenerators.remove(buttonGenerator);
	}

	@Override
	public Collection<ButtonGenerator> getButtonGenerators() {
		return buttonGenerators;
	}


    @Override
    public AbstractTaskListView getTasksListView(String viewName)
    {
       return tasksListViews.get(viewName);
    }

    @Override
    public List<AbstractTaskListView> getTasksListViews(String currentUserLogin)
    {
        List<AbstractTaskListView> userViews = new LinkedList<AbstractTaskListView>();

        UserData user = userSource.getUserByLogin(currentUserLogin);

        if(user == null)
            throw new RuntimeException("No user with given login="+currentUserLogin);


        for(AbstractTaskListView taskListView: tasksListViews.values())
        {
            boolean userHasPrivilegesToSeeView = false;
            /* No role is reuqired */
            if(taskListView.getRoleNames().isEmpty())
            {
                userHasPrivilegesToSeeView = true;
            }
            /* Has user any of required roles to see view? */
            else
            {
                Set<String> rolesIntercestion = new HashSet<String>(taskListView.getRoleNames());
                rolesIntercestion.retainAll(user.getRoles());
                if(!rolesIntercestion.isEmpty())
                    userHasPrivilegesToSeeView = true;
            }

            /* User is not privileged to see view, go to the next */
            if(!userHasPrivilegesToSeeView)
                continue;

            userViews.add(taskListView);
        }

        /* Sort by proprity */
        Collections.sort(userViews);

        return userViews;
    }

	@Override
	public void registerTaskPermissionChecker(TaskPermissionChecker permissionChecker) {
		taskPermissionCheckers.add(permissionChecker);
	}

	@Override
	public void unregisterTaskPermissionChecker(TaskPermissionChecker permissionChecker) {
		taskPermissionCheckers.remove(permissionChecker);
	}

	@Override
	public List<TaskPermissionChecker> getTaskPermissionCheckers() {
		return Collections.unmodifiableList(taskPermissionCheckers);
	}

	@Override
	public void registerActionPermissionChecker(ActionPermissionChecker permissionChecker) {
		actionPermissionCheckers.add(permissionChecker);
	}

	@Override
	public void unregisterActionPermissionChecker(ActionPermissionChecker permissionChecker) {
		actionPermissionCheckers.remove(permissionChecker);
	}

	@Override
	public List<ActionPermissionChecker> getActionPermissionCheckers() {
		return Collections.unmodifiableList(actionPermissionCheckers);
	}

	@Override
    public void registerTasksListView(String viewName, AbstractTaskListView taskListView) {
        tasksListViews.put(viewName, taskListView);

        try
        {
            InputStream htmlFileStream = taskListView.getContentProvider().getHtmlContent();
            String htmlBody = CharStreams.toString(new InputStreamReader(htmlFileStream, "UTF-8"));

            templateProvider.addTemplate(viewName, htmlBody);
        }
        catch(Exception ex)
        {
            throw new RuntimeException("Problem during adding new html template", ex);
        }

        logger.info("Registered tasks list view: " + viewName);
    }

    @Override
    public void unregisterTasksListView(String viewName) {
        tasksListViews.remove(viewName);

        templateProvider.removeTemplate(viewName);

        logger.info("Unregistered tasks list view: " + viewName);
    }

	private static String compress(InputStream stream)
	{
		try
		{
			ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
			OutputStream output = new GZIPOutputStream(byteOutput);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;

			while ((bytesRead = stream.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}

			output.close();
			byteOutput.close();

			return byteOutput.toString();
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Problem during javascript compressing", ex);
		}
	}

	private static String decompress(String stringToCompress)
	{
		try
		{
			final int BUFFER_SIZE = 32;
			ByteArrayInputStream is = new ByteArrayInputStream(stringToCompress.getBytes());
			GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
			StringBuilder string = new StringBuilder();
			byte[] data = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = gis.read(data)) != -1) {
				string.append(new String(data, 0, bytesRead));
			}
			gis.close();
			is.close();
			return string.toString();
		}
		catch(IOException ex)
		{
			throw new RuntimeException("Problem during javascript decompressing", ex);
		}
	}

	private static void checkClassFound(String name, Class clazz) {
		if (clazz == null) {
			throw new RuntimeException("No class nicknamed by: " + name);
		}
	}

    @Override
    public synchronized Collection<GenericPortletViewRenderer> getGenericPortletViews(String portletKey) {
        return genericPortletViewRenderers.containsKey(portletKey)
                ? genericPortletViewRenderers.get(portletKey)
                : Collections.<GenericPortletViewRenderer>emptyList();
    }

    @Override
    public synchronized void registerGenericPortletViewRenderer(String portletKey, GenericPortletViewRenderer renderer) {
        if (!genericPortletViewRenderers.containsKey(portletKey)) {
            genericPortletViewRenderers.put(portletKey, new HashSet<GenericPortletViewRenderer>());
        }
        genericPortletViewRenderers.get(portletKey).add(renderer);
    }

    @Override
    public synchronized void unregisterGenericPortletViewRenderer(String portletKey, GenericPortletViewRenderer renderer) {
        if (genericPortletViewRenderers.containsKey(portletKey)) {
            genericPortletViewRenderers.get(portletKey).remove(renderer);
        }
    }
}
