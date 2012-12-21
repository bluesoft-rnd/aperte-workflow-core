package org.aperteworkflow.editor.processeditor.tab.message;

import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.Language;
import org.aperteworkflow.editor.vaadin.DataHandler;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Lang;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MessageEditor extends VerticalLayout implements TabSheet.CloseHandler, DataHandler {
	/**
     * The window which allows to define and add new language to the editor
     */
    private class NewLanguageWindow extends Window {

        private LanguageForm languageForm;
        private Button addButton;
        
        public NewLanguageWindow() {
            initComponent();
            initLayout();
        }

        private void initComponent() {
            I18NSource messages  = I18NSource.ThreadUtil.getThreadI18nSource();

            setCaption(messages.getMessage("messages.language.new.caption"));
            
            languageForm = new LanguageForm();

            addButton = VaadinUtility.button(
                    messages.getMessage("messages.language.new.add"),
                    new Runnable() {
                        @Override
                        public void run() {
                            languageForm.commit();

                            Language lang = languageForm.getLanguage();
                            if (languageProperties.containsKey(lang)) {
                                I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
                                VaadinUtility.errorNotification(
                                        getApplication(),
                                        messages,
                                        messages.getMessage("messages.language.new.exists")
                                );
                                return;
                            }

                            addLanguageTab(lang, null);
                            NewLanguageWindow.this.close();
                        }
                    }
            );
        }

        private void initLayout() {
           

            addComponent(languageForm);
            addComponent(addButton);

            setModal(true);
            setSpacing(true);
        }
    }

    private Map<Language, String> languageMessages;
    private Map<Language, PropertiesArea> languageProperties;
    private Button newLanguageButton;
	private Select defaultLanguageSelect;
    private Label languageDescriptionLabel;
    private TabSheet languageTabs;

    public MessageEditor() {
        languageProperties = new HashMap<Language, PropertiesArea>();
        languageMessages = new HashMap<Language, String>();
        initComponent();
        initLayout();
    }

    public Map<Language, String> getLanguageMessages() {
        return languageMessages;
    }

    public void setLanguageMessages(Map<Language, String> languageMessages) {
        this.languageMessages = languageMessages;
		if (languageMessages != null) {
			for (Language language : languageMessages.keySet()) {
				addAvailableDefaultLanguage(language.getCode());
			}
		}
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
                removeLanguageTab(tabContent);
            }
        });

        // size the message window, otherwise it looks silly
        confirmWindow.setWidth("300px");
        confirmWindow.getContent().setSizeFull();

        getApplication().getMainWindow().addWindow(confirmWindow);
    }

    private void initLayout() {
        setSpacing(true);
        addComponent(languageDescriptionLabel);
        addComponent(newLanguageButton);
		addComponent(defaultLanguageSelect);
        addComponent(languageTabs);
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        newLanguageButton = VaadinUtility.button(
                messages.getMessage("messages.language.new.button"),
                new Runnable() {
                    @Override
                    public void run() {
                        Window window = new NewLanguageWindow();
                        MessageEditor.this.getApplication().getMainWindow().addWindow(window);
                    }
                }
        );

		defaultLanguageSelect = new Select(messages.getMessage("messages.default.language"));
		defaultLanguageSelect.setWidth(150, UNITS_PIXELS);

        languageDescriptionLabel = new Label(messages.getMessage("messages.language.description"));

        languageTabs = new TabSheet();
        languageTabs.setCloseHandler(this);
    }

    private void addLanguageTab(Language language, String messagesContent) {
        PropertiesArea area = new PropertiesArea();
        area.setValue(messagesContent);
        area.setLanguage(language);
        languageProperties.put(language, area);

        TabSheet.Tab tab = languageTabs.addTab(area, language.getCode());
        tab.setClosable(true);

		addAvailableDefaultLanguage(language.getCode());
		if (getAvailableDefaultLanguageCount() == 1 && getDefaultLanguage() == null) {
			setDefaultLanguage(language.getCode());
		}
    }

	private void removeLanguageTab(Component c) {
        PropertiesArea area = (PropertiesArea) c;
        Language language = area.getLanguage();
        languageProperties.remove(language);

        languageTabs.removeComponent(c);

		if (Lang.equals(getDefaultLanguage(), language.getCode())) {
			setDefaultLanguage(null);
		}
		removeAvailableDefaultLanguage(language.getCode());
	}

	public String getDefaultLanguage() {
		return (String)defaultLanguageSelect.getValue();
	}

	public void setDefaultLanguage(String languageCode) {
		defaultLanguageSelect.setValue(languageCode);
	}

	private void addAvailableDefaultLanguage(String languageCode) {
		defaultLanguageSelect.getContainerDataSource().addItem(languageCode);
	}

	private void removeAvailableDefaultLanguage(String languageCode) {
		defaultLanguageSelect.getContainerDataSource().removeItem(languageCode);
	}

	private int getAvailableDefaultLanguageCount() {
		return defaultLanguageSelect.getContainerDataSource().getItemIds().size();
	}

	@Override
    public void loadData() {
        languageProperties.clear();
        languageTabs.removeAllComponents();

        if (languageMessages == null || languageMessages.isEmpty()) {
            // nothing to load
            return;
        }

        for (Language lang : languageMessages.keySet()) {
            String messagesContent = Native2AsciiUtil.ascii2Native(languageMessages.get(lang));
            addLanguageTab(lang, messagesContent);
        }
    }

    @Override
    public void saveData() {
        languageMessages = null; // don't use clear(), we can get null from the outside

        if (languageProperties == null || languageProperties.isEmpty()) {
            // nothing to save
            return;
        }

        languageMessages = new HashMap<Language, String>();
        for (Language lang : languageProperties.keySet()) {
            PropertiesArea area = languageProperties.get(lang);
            String messagesContent = Native2AsciiUtil.native2Ascii((String) area.getValue());
            languageMessages.put(lang, messagesContent);
        }
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

}
