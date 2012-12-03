package org.aperteworkflow.editor.processeditor.tab.message;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.Language;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryLoader;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
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
			try {
				Object obj = DictionaryLoader.getInstance().unmarshall(new ByteArrayInputStream(processConfig.getDictionary().getBytes("UTF-8")));
				System.out.println(obj.getClass());
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
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
