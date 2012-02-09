package org.aperteworkflow.editor.processeditor;

import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Window;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.json.ProcessConfigJSONHandler;
import org.aperteworkflow.editor.stepeditor.JavaScriptHelper;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Map;

/**
 * @author kdowbecki@bluesoft.net.pl
 */
public class ProcessEditorApplication extends GenericEditorApplication implements ParameterHandler {

    private static final String CALLBACK_URL_PARAM_NAME = "callbackUrl";
    private static final String PROCESS_CONFIG_PARAM_NAME = "processConfig";

    private ProcessConfig processConfig;
    
    private String callbackUrl;
    private String jsonProcessConfig;
    private Window mainWindow;
    private ProcessEditorPanel processEditorPanel;
    private JavaScriptHelper mainWindowJavaScriptHelper;

    @Override
    public void init() {
        super.init();

        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        mainWindow = new Window(messages.getMessage("process.editor.title"));
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
        if (callbackUrl == null) {
            return;
        }

        this.callbackUrl = callbackUrl;
        jsonProcessConfig = getStringParameterByName(PROCESS_CONFIG_PARAM_NAME, parameters);
        refreshApplication();
    }

    /**
     * Reinitialize the application from the last received configuration
     */
    private void refreshApplication() {
        mainWindow.removeAllComponents();

        processConfig = ProcessConfigJSONHandler.getInstance().toObject(jsonProcessConfig);

        processEditorPanel = new ProcessEditorPanel();
        processEditorPanel.setProcessConfig(processConfig);
        processEditorPanel.loadData();

        mainWindow.addComponent(processEditorPanel);
    }

    /**
     * Save and callback the signavio editor
     */
    public void saveAndCallback() {
        processEditorPanel.saveData();
        String json = ProcessConfigJSONHandler.getInstance().toJSON(processConfig);
        mainWindowJavaScriptHelper.postAndRedirectProcess(callbackUrl, json);
    }

}
