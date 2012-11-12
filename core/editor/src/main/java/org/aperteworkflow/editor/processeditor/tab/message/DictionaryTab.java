package org.aperteworkflow.editor.processeditor.tab.message;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.Language;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
        if (processConfig.getDictionary() == null) {
            editor.setDictionaryToUpload(null);
        } else {
            editor.setDictionaryToUpload(processConfig.getDictionary());
        }
        editor.loadData();
    }

    @Override
    public void saveData() {
        editor.saveData();
        if (editor.getDictionaryToUpload() == null) {
            processConfig.setDictionary(null);
        } else {
            processConfig.setDictionary(editor.getDictionaryToUpload());
        }
    }

    @Override
    public Collection<String> validateData() {
        return editor.validateData();
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

}
