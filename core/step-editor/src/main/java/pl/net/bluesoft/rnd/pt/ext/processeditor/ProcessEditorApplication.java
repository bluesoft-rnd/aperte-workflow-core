package pl.net.bluesoft.rnd.pt.ext.processeditor;

import com.vaadin.terminal.ParameterHandler;
import com.vaadin.ui.Window;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.domain.ProcessModelConfig;
import org.aperteworkflow.editor.json.ProcessConfigJSONHandler;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.JavaScriptHelper;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Map;

/**
 * @author kdowbecki@bluesoft.net.pl
 */
public class ProcessEditorApplication extends GenericEditorApplication implements ParameterHandler {

    private static final String CALLBACK_URL_PARAM_NAME = "callbackUrl";
    private static final String PROCESS_CONFIG_PARAM_NAME = "processConfig";
    private static final String PROCESS_MODEL_DIRECTORY = "processModelDirectory";
    private static final String PROCESS_MODEL_FILE_NAME = "processModelFileName";
    private static final String PROCESS_MODEL_NEW = "processModelNew";
    private static final String MODELER_REPO_DIRECTORY = "modelerRepoDirectory";

    private ProcessConfig processConfig;
    private ProcessModelConfig processModelConfig;
    
    private String callbackUrl;
    private String jsonProcessConfig;
    private String processModelFileName;
    private String processModelDirectory;
    private String processModelNew;
    private String modelerRepoDirectory;
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
        processModelFileName = getStringParameterByName(PROCESS_MODEL_FILE_NAME, parameters);
        processModelDirectory = getStringParameterByName(PROCESS_MODEL_DIRECTORY, parameters);
        processModelNew = getStringParameterByName(PROCESS_MODEL_NEW, parameters);
        modelerRepoDirectory = getStringParameterByName(MODELER_REPO_DIRECTORY, parameters);
        refreshApplication();
    }

    /**
     * Reinitialize the application from the last received configuration
     */
    private void refreshApplication() {
        mainWindow.removeAllComponents();

        processConfig = ProcessConfigJSONHandler.getInstance().toObject(jsonProcessConfig);
        processModelConfig = new ProcessModelConfig();
        processModelConfig.setDirectory(processModelDirectory);
        processModelConfig.setFileName(processModelFileName);
        processModelConfig.setModelerRepoDirectory(modelerRepoDirectory);
        // tricky, signavio may not set it for saved model
        processModelConfig.setNewModel("true".equals(processModelNew));

        processEditorPanel = new ProcessEditorPanel();
        processEditorPanel.setProcessModelConfig(processModelConfig);
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
