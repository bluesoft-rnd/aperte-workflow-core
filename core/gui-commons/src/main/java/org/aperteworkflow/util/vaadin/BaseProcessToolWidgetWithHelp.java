package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.ui.*;
import org.aperteworkflow.ui.help.HelpProvider;
import org.aperteworkflow.ui.help.HelpProviderFactory;

import org.hibernate.Hibernate;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.service.UserFinder;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class BaseProcessToolWidgetWithHelp extends BaseProcessToolWidget implements UserFinder {
    protected HelpProvider helpProvider;
    private String helpDictionaryName;

    protected BaseProcessToolWidgetWithHelp(String helpDictionaryName) {
        this.helpDictionaryName = helpDictionaryName;
    }

    private void initHelpProvider() {
        if (helpProvider == null) {
			HelpProviderFactory helpProviderFactory = (HelpProviderFactory)ProcessToolContext.Util
					.getThreadProcessToolContext()
					.getRegistry()
					.lookupService(HelpProviderFactory.class.getName());
			helpProvider = helpProviderFactory
					.getInstance(getApplication(), getProcessDefinitions(), !cannotEdit(), helpDictionaryName);
            if (helpProvider == null) {
                helpProvider = createDummyHelpProvider();
            }
        }
    }

    public static HelpProvider createDummyHelpProvider() {
        return new HelpProvider() {
            @Override
            public Component helpIcon(String taskName, String s) {
                return new Label("no help loaded");
            }

            @Override
            public Component getHelpIcon(String key) {
                return new Label("no help loaded");
            }

            @Override
            public Component getHelpIcon(String key, String message) {
                return new Label("no help loaded");
            }

            @Override
            public Field wrapFieldWithHelp(Field field, String key) {
                return new TextField("no help loaded");
            }

            @Override
            public void prepare(Application application, List<ProcessDefinitionConfig> cfgs, boolean canEdit, String helpDictionaryName) {
            }

            @Override
            public Component wrapComponentWithHelp(Component component, String key) {
                return new Label("no help loaded");
            }

            @Override
            public Component wrapComponentWithHelp(Component component, String key, String iconPlacement, String popupPlacement) {
                return new Label("no help loaded");
            }

            @Override
            public void attachToLayout(Layout layout) {
                layout.addComponent(new Label("no help loaded"));
            }

            @Override
            public Field getFieldWithHelp(Field wrappedField, Component helpButton) {
                return new TextField("no help provided");
            }

            @Override
            public void makeTableHelpEnabled(Table t) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void addHelpForColumn(Table t, Object propertyId, String key) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Field stripFieldFromHelp(Field f) {
                return f;
            }

            @Override
            public void makeTableHelpEnabled(Table t, Component helpPosition) {
                //nothing
            }

            @Override
            public void showHelpFor(Component component) {
                //nothing
            }
        };
    }

    public HelpProvider getHelpProvider() {
        initHelpProvider();
        return helpProvider;
    }

    public void setHelpProvider(HelpProvider helpProvider) {
        this.helpProvider = helpProvider;
    }

    public Component getHelpIcon(String key) {
        if (cannotEdit()) return new Label("");
        initHelpProvider();
        return helpProvider.getHelpIcon(key);
    }

    public Component getHelpIcon(String key, String message) {
        if (cannotEdit()) return new Label("");
        initHelpProvider();
        return helpProvider.helpIcon(key, message);
    }

    private boolean cannotEdit() {
        return !hasPermission("EDIT");
    }

    public Field wrapFieldWithHelp(Field field, String key) {
        if (cannotEdit()) return field;
        initHelpProvider();
        return helpProvider.wrapFieldWithHelp(field, key);
    }

    public Component wrapComponentWithHelp(Component component, String key) {
        if (cannotEdit()) return component;
        initHelpProvider();
        return helpProvider.wrapComponentWithHelp(component, key);
    }

    public Component wrapComponentWithHelp(Component component, String key, String iconPlacement, String popupPlacement) {
        if (cannotEdit()) return component;
        initHelpProvider();
        return helpProvider.wrapComponentWithHelp(component, key, iconPlacement, popupPlacement);
    }

    public String getHelpDictionaryName() {
        return helpDictionaryName;
    }

    /**
     * @deprecated contextHelpIsAttachedToMainWindow
     */
    @Deprecated
    protected void attachContextHelpToLayout(){};

    protected abstract ProcessDefinitionConfig getProcessDefinition();

	protected List<ProcessDefinitionConfig> getProcessDefinitions() {
		return Arrays.asList(getProcessDefinition());
	}

	protected List<ProcessDefinitionConfig> upToRootInstance(ProcessInstance processInstance) {
		List<ProcessDefinitionConfig> definitions = new ArrayList<ProcessDefinitionConfig>();
		while (processInstance != null) {
			Hibernate.initialize(processInstance.getDefinition());
			definitions.add(processInstance.getDefinition());
			processInstance = processInstance.getParent();
		}
		return definitions;
	}

    public UserData getUserByLogin(String login) {
        if (login != null && getApplication() instanceof GenericVaadinPortlet2BpmApplication) {
            return ((GenericVaadinPortlet2BpmApplication)getApplication()).getUser(login);
        }
        return null;
    }

    public UserData getUserByEmail(String email) {
        if (email != null && getApplication() instanceof GenericVaadinPortlet2BpmApplication) {
            return ((GenericVaadinPortlet2BpmApplication)getApplication()).getUserByEmail(email);
        }
        return null;
    }
}
