package org.aperteworkflow.editor.processeditor;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.processeditor.tab.definition.ProcessDefinitionTab;
import org.aperteworkflow.editor.processeditor.tab.message.MessageTab;
import org.aperteworkflow.editor.processeditor.tab.message.DictionaryTab;
import org.aperteworkflow.editor.processeditor.tab.other.OtherTab;
import org.aperteworkflow.editor.processeditor.tab.permission.ProcessPermissionTab;
import org.aperteworkflow.editor.processeditor.tab.queue.QueueTab;
import org.aperteworkflow.editor.vaadin.DataHandler;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.Iterator;

/**
 * Main panel for process editor application
 */
public class ProcessEditorPanel extends GridLayout implements DataHandler {

    private TabSheet tabSheet;

    private OtherTab otherTab;
    private QueueTab queueTab;
    private ProcessPermissionTab permissionTab;
    private MessageTab messageTab;
    private DictionaryTab dictionaryTab;
    private ProcessDefinitionTab processDefinitionTab;

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
        tabSheet.addTab(processDefinitionTab = new ProcessDefinitionTab(), messages.getMessage("process.editor.process.definition"));
        tabSheet.addTab(queueTab = new QueueTab(), messages.getMessage("process.editor.queues"));
        tabSheet.addTab(messageTab = new MessageTab(), messages.getMessage("process.editor.messages"));
        tabSheet.addTab(dictionaryTab = new DictionaryTab(), messages.getMessage("process.editor.dictionary"));
        tabSheet.addTab(otherTab = new OtherTab(), messages.getMessage("process.editor.other"));

        saveButton = VaadinUtility.button(messages.getMessage("process.editor.save"), new Runnable() {
            @Override
            public void run() {
                ((ProcessEditorApplication) ProcessEditorApplication.getCurrent()).saveAndCallback();
            }
        });
        
        titleLabel = new Label(messages.getMessage("process.editor.title"));
        titleLabel.addStyleName("h1");
    }

    @Override
    public void loadData() {
        Iterator<Component> it = tabSheet.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof DataHandler) {
                ((DataHandler) c).loadData();
            }
        }
    }

    @Override
    public void saveData() {
        Iterator<Component> it = tabSheet.getComponentIterator();
        while (it.hasNext()) {
            Component c = it.next();
            if (c instanceof DataHandler) {
                ((DataHandler) c).saveData();
            }
        }
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        permissionTab.setProcessConfig(processConfig);
        queueTab.setProcessConfig(processConfig);
        messageTab.setProcessConfig(processConfig);
        dictionaryTab.setProcessConfig(processConfig);
        otherTab.setProcessConfig(processConfig);
        processDefinitionTab.setProcessConfig(processConfig);
    }
}
