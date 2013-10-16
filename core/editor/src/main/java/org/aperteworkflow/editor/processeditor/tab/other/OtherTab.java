package org.aperteworkflow.editor.processeditor.tab.other;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

public class OtherTab extends VerticalLayout implements DataHandler {
	private ProcessConfig processConfig;

    private ProcessLogoEditor processLogoEditor;
	private Label defaultStepInfoLabel;
	private TextField defaultStepInfoField;

    public OtherTab() {
        initComponents();

        setMargin(true);
        addComponent(processLogoEditor);
		addComponent(defaultStepInfoLabel);
		addComponent(defaultStepInfoField);
    }
    
    private void initComponents() {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

		processLogoEditor = new ProcessLogoEditor();

		defaultStepInfoLabel = new Label(messages.getMessage("process.defaultStepInfo.description"));

		defaultStepInfoField = new TextField();
		defaultStepInfoField.setWidth("100%");
		defaultStepInfoField.setNullRepresentation("");
    }

    public void setProcessConfig(ProcessConfig processConfig) {
		this.processConfig = processConfig;
        processLogoEditor.setProcessConfig(processConfig);
    }

    @Override
    public void loadData() {
        processLogoEditor.loadData();
		defaultStepInfoField.setValue(processConfig.getDefaultStepInfo());
    }

    @Override
    public void saveData() {
        processLogoEditor.saveData();
		processConfig.setDefaultStepInfo((String)defaultStepInfoField.getValue());
    }

    @Override
    public Collection<String> validateData() {
        return processLogoEditor.validateData();
    }
}
