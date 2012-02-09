package org.aperteworkflow.editor.processeditor.tab.other;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;

import java.util.Collection;

public class OtherTab extends VerticalLayout implements DataHandler {

    private ProcessLogoEditor processLogoEditor;

    public OtherTab() {
        processLogoEditor = new ProcessLogoEditor();

        setMargin(true);
        addComponent(processLogoEditor);
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        processLogoEditor.setProcessConfig(processConfig);
    }

    @Override
    public void loadData() {
        processLogoEditor.loadData();
    }

    @Override
    public void saveData() {
        processLogoEditor.saveData();
    }

    @Override
    public Collection<String> validateData() {
        return processLogoEditor.validateData();
    }
}
