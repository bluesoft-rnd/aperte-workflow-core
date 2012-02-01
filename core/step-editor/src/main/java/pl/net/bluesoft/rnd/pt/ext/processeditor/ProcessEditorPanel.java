package pl.net.bluesoft.rnd.pt.ext.processeditor;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.domain.ProcessModelConfig;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.message.MessageTab;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other.OtherTab;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.permission.ProcessPermissionTab;
import pl.net.bluesoft.rnd.pt.ext.processeditor.tab.queue.QueueTab;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.Collection;

/**
 * Main panel for process editor application
 */
public class ProcessEditorPanel extends GridLayout implements DataHandler {

    private TabSheet tabSheet;
    private OtherTab otherTab;
    private QueueTab queueTab;
    private ProcessPermissionTab permissionTab;
    private MessageTab messageTab;
    private Label titleLabel;
    private Button saveButton;

    public ProcessEditorPanel() {
        super(2, 2);
        initComponents();
        initLayout();
    }

    private void initLayout() {
        setSpacing(true);
        setWidth("100%");

        addComponent(titleLabel, 0, 0);
        addComponent(saveButton, 1, 0);
        addComponent(tabSheet, 0, 1, 1, 1);

        setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT);
        setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
        
        setColumnExpandRatio(0, 1);
        setColumnExpandRatio(1, 0);
    }

    private void initComponents() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.addTab(permissionTab = new ProcessPermissionTab(), messages.getMessage("process.editor.process.permissions"));
        tabSheet.addTab(queueTab = new QueueTab(), messages.getMessage("process.editor.queues"));
        tabSheet.addTab(messageTab = new MessageTab(), messages.getMessage("process.editor.messages"));
        tabSheet.addTab(otherTab = new OtherTab(), messages.getMessage("process.editor.other"));

        saveButton = VaadinUtility.button(messages.getMessage("process.editor.save"), new Runnable() {
            @Override
            public void run() {
                ((ProcessEditorApplication) ProcessEditorApplication.getCurrent()).saveAndCallback();
            }
        });
        
        titleLabel = new Label("<h1>" + messages.getMessage("process.editor.title") + "</h1>");
        titleLabel.setContentMode(Label.CONTENT_XHTML);
    }

    @Override
    public void loadData() {
        permissionTab.loadData();
        queueTab.loadData();
        otherTab.loadData();
        messageTab.loadData();
    }

    @Override
    public void saveData() {
        permissionTab.saveData();
        queueTab.saveData();
        otherTab.saveData();
        messageTab.saveData();
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public void setProcessModelConfig(ProcessModelConfig processModelConfig) {
        otherTab.setProcessModelConfig(processModelConfig);
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        permissionTab.setProcessConfig(processConfig);
        queueTab.setProcessConfig(processConfig);
        messageTab.setProcessConfig(processConfig);
    }
}
