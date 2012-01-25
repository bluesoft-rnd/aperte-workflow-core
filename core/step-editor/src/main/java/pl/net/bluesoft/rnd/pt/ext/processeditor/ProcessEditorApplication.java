package pl.net.bluesoft.rnd.pt.ext.processeditor;

import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Window;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.JavaScriptHelper;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.Map;

/**
 * @author kdowbecki@bluesoft.net.pl
 */
public class ProcessEditorApplication extends GenericEditorApplication implements ParameterHandler {

    private static final String CALLBACK_URL_PARAM_NAME = "callbackUrl";
    private static final String PROCESS_CONFIG_PARAM_NAME = "processConfig";

    private String callbackUrl;
    private String processConfig;
    private Window mainWindow;
    private ProcessEditorPanel processEditorPanel;
    private JavaScriptHelper mainWindowJavaScriptHelper;

    @Override
    public void init() {
        super.init();

        I18NSource messages = VaadinUtility.getThreadI18nSource();

        mainWindow = new Window(messages.getMessage("processeditor.application.name"));
        mainWindow.addParameterHandler(this);

        mainWindowJavaScriptHelper = new JavaScriptHelper(mainWindow);
        mainWindowJavaScriptHelper.preventWindowClosing();

        setMainWindow(mainWindow);
    }
    
    @Override
    public void handleParameters(Map<String, String[]> parameters) {
        if (parameters.isEmpty()) {
            return;
        }

        String callbackUrl = getStringParameterByName(CALLBACK_URL_PARAM_NAME, parameters);
        String processConfig = getStringParameterByName(PROCESS_CONFIG_PARAM_NAME, parameters);
        if (callbackUrl != null) {
            this.callbackUrl = callbackUrl;
            this.processConfig = processConfig;
            refreshApplication();
        }
    }

    private void refreshApplication() {
        mainWindow.removeAllComponents();

        processEditorPanel = new ProcessEditorPanel();
        mainWindow.addComponent(processEditorPanel);
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getProcessConfig() {
        return processConfig;
    }

    public JavaScriptHelper getJavaScriptHelper() {
        return mainWindowJavaScriptHelper;
    }
}
