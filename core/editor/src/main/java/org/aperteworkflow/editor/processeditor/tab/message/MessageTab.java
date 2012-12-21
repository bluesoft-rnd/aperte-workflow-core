package org.aperteworkflow.editor.processeditor.tab.message;

import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.editor.domain.Language;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.vaadin.DataHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MessageTab extends VerticalLayout implements DataHandler {

    private ProcessConfig processConfig;

    private MessageEditor editor;

    public MessageTab() {
        initComponent();
    }

    private void initComponent() {
        editor = new MessageEditor();

        setMargin(true);
        addComponent(editor);
    }

    @Override
    public void loadData() {
        if (processConfig.getMessages() == null) {
            editor.setLanguageMessages(null);
			editor.setDefaultLanguage(null);
        } else {
            Map<Language, String> messages = new HashMap<Language, String>();
            for (String langCode : processConfig.getMessages().keySet()) {
                Language lang = new Language();
                lang.setCode(langCode);
                messages.put(lang, processConfig.getMessages().get(langCode));
            }
            editor.setLanguageMessages(messages);
			editor.setDefaultLanguage(processConfig.getDefaultLanguage());
        }

        editor.loadData();
    }

    @Override
    public void saveData() {
        editor.saveData();

        if (editor.getLanguageMessages() == null) {
            processConfig.setMessages(null);
			processConfig.setDefaultLanguage(null);
        } else {
            Map<String, String> messages = new HashMap<String, String>();
            for (Language lang : editor.getLanguageMessages().keySet()) {
                messages.put(lang.getCode(), editor.getLanguageMessages().get(lang));
            }
            processConfig.setMessages(messages);
			processConfig.setDefaultLanguage(editor.getDefaultLanguage());
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
