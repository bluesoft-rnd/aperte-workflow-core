package org.aperteworkflow.editor.processeditor.tab.queue;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.ui.queue.QueueEditor;
import org.aperteworkflow.editor.vaadin.DataHandler;

import java.util.Collection;

public class QueueTab extends VerticalLayout implements DataHandler {

    private ProcessConfig processConfig;
    private ProcessQueueProvider processQueueProvider;

    private QueueEditor queueEditor;

    public QueueTab() {
        initComponents();
        initLayout();
    }

    private void initComponents() {
        processQueueProvider = new ProcessQueueProvider();

        queueEditor = new QueueEditor();
        queueEditor.setProvider(processQueueProvider);
    }

    private void initLayout() {
        setMargin(true);
        addComponent(queueEditor);
    }

    @Override
    public void loadData() {
        processQueueProvider.setQueues(processConfig.getQueues());

        queueEditor.loadData();
    }

    @Override
    public void saveData() {
        queueEditor.saveData();

        processConfig.setQueues(processQueueProvider.getQueues());
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }
}
