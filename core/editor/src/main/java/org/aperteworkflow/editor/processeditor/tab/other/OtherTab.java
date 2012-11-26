package org.aperteworkflow.editor.processeditor.tab.other;

import static org.aperteworkflow.util.vaadin.VaadinUtility.styled;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.processeditor.ProcessEditorApplication;
import org.aperteworkflow.editor.vaadin.DataHandler;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

public class OtherTab extends VerticalLayout implements DataHandler {
	private ProcessConfig processConfig;
    private ProcessLogoEditor processLogoEditor;
    private Label taskItemClassLabel;
    private Select taskItemClassField;

    public OtherTab() {
        processLogoEditor = new ProcessLogoEditor();
        initComponents();

        setMargin(true);
        addComponent(processLogoEditor);
        addComponent(taskItemClassLabel);
        addComponent(taskItemClassField);
    }
    
    private void  initComponents(){
    	I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
    	taskItemClassLabel = styled(new Label(messages.getMessage("process.definition.taskItemClass")), "h2");
		taskItemClassField = new Select();
        taskItemClassField.setNewItemsAllowed(true);
        taskItemClassField.setWidth("100%");
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        processLogoEditor.setProcessConfig(processConfig);
        this.processConfig=processConfig;
    }

    @Override
    public void loadData() {
        processLogoEditor.loadData();
		ProcessToolRegistry registry = ((ProcessEditorApplication)ProcessEditorApplication.getCurrent()).getRegistry();
		for (String taskItemClass : from(registry.getAvailableTaskItemProviders().keySet()).ordered()) {
			taskItemClassField.addItem(taskItemClass);
		}
		taskItemClassField.setValue(processConfig.getTaskItemClass());
    }

    @Override
    public void saveData() {
        processLogoEditor.saveData();
		processConfig.setTaskItemClass((String)taskItemClassField.getValue());
    }

    @Override
    public Collection<String> validateData() {
        return processLogoEditor.validateData();
    }
}
