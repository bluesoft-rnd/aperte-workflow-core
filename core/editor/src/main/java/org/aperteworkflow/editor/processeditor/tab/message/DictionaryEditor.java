package org.aperteworkflow.editor.processeditor.tab.message;

import com.vaadin.ui.*;

import org.aperteworkflow.editor.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;


public class DictionaryEditor extends VerticalLayout implements TabSheet.CloseHandler, DataHandler {


    private String dictionaryToUpload;
    private Label languageDescriptionLabel;
    private TextArea textArea;
    
    private final static String DEFAULT_TEXT_AREA_STRING ="";

    public DictionaryEditor() {
        initComponent();
        initLayout();
    }

	@Override
    public void onTabClose(TabSheet tabsheet, final Component tabContent) {
        I18NSource messages  = I18NSource.ThreadUtil.getThreadI18nSource();

        String langCode = "UNKNOWN";
        if (tabContent instanceof PropertiesArea) {
            PropertiesArea props = (PropertiesArea) tabContent;
            langCode = props.getLanguage().getCode();
        }

        ConfirmWindow confirmWindow = new ConfirmWindow();
        confirmWindow.setMessageValue(messages.getMessage(
                "messages.language.delete.warning",
                new Object[]{langCode})
        );
        confirmWindow.setConfirmCaption(messages.getMessage("messages.language.delete.yes"));
        confirmWindow.setCancelCaption(messages.getMessage("messages.language.delete.no"));
        confirmWindow.addConfirmListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
            }
        });

        // size the message window, otherwise it looks silly
        confirmWindow.setWidth("300px");
        confirmWindow.getContent().setSizeFull();

        getApplication().getMainWindow().addWindow(confirmWindow);
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
        languageDescriptionLabel = new Label(messages.getMessage("messages.dictionary.description"));
        addDictionaryTextArea(DEFAULT_TEXT_AREA_STRING);
        
    } 
    
    private void initLayout() {
        setSpacing(true);
        addComponent(languageDescriptionLabel);
        addComponent(textArea);
    }

    private void addDictionaryTextArea(String messagesContent) {
    	if(textArea==null){
         textArea = new TextArea();
        textArea.setRows(20);
        textArea.setWidth("100%");
    	}
        textArea.setValue(messagesContent);
    }


    @Override
    public void loadData() {
        if (dictionaryToUpload == null || dictionaryToUpload.isEmpty()) {
            // nothing to load
            return;
        }
        addDictionaryTextArea(dictionaryToUpload);
    }

    @Override
    public void saveData() {
    	String textAreaValue=(String) textArea.getValue();
        if (textAreaValue == null || textAreaValue.isEmpty()) {
            // nothing to save
            return;
        }
        dictionaryToUpload = textAreaValue;
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
    
    public String getDictionaryToUpload() {
  		return dictionaryToUpload;
  	}

  	public void setDictionaryToUpload(String dictionaryToUpload) {
  		this.dictionaryToUpload = dictionaryToUpload;
  	}

}
