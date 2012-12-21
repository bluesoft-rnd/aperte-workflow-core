package org.aperteworkflow.editor.processeditor.tab.dict;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;

import java.util.Collection;

public class DictionaryTab extends VerticalLayout implements DataHandler {

    private ProcessConfig processConfig;

    private DictionaryEditor editor;

    public DictionaryTab() {
        initComponent();
    }

    private void initComponent() {
        editor = new DictionaryEditor();
        setMargin(true);
        addComponent(editor);
    }

    @Override
    public void loadData() {
		editor.setDictionary(processConfig.getDictionary());
        editor.loadData();
    }

	@Override
    public void saveData() {
        editor.saveData();
		processConfig.setDictionary(editor.getDictionary());
    }

    @Override
    public Collection<String> validateData() {
        return editor.validateData();
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

}
