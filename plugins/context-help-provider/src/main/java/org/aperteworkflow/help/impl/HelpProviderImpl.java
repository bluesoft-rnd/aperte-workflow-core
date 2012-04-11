package org.aperteworkflow.help.impl;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import org.aperteworkflow.ui.help.HelpProvider;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.Placement;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class HelpProviderImpl implements HelpProvider {

    private HelpFactory helpFactory;
    private boolean canEdit;

    @Override
    public void prepare(Application application, ProcessDefinitionConfig cfg, boolean canEdit, String helpDictionaryName) {
        ContextHelp contextHelp = new ContextHelp();
        this.canEdit = canEdit;
        application.getMainWindow().getContent().addComponent(contextHelp);
        helpFactory = new HelpFactory(
                cfg,
                application,
                I18NSource.ThreadUtil.getThreadI18nSource(),
//                "step_help",
                helpDictionaryName,
                contextHelp);
    }

    @Override
    public Component helpIcon(String taskName, String s) {
        return helpFactory.helpIcon(taskName, s);
    }

    @Override
    public Component getHelpIcon(String key) {
   		if (cannotEdit()) return new Label("");
   		return helpFactory.helpIcon(key);
   	}

    private boolean cannotEdit() {
        return !canEdit;
    }

    @Override
    public Component getHelpIcon(String key, String message) {
   		if (cannotEdit()) return new Label("");
   		return helpFactory.helpIcon(key, message);
   	}

    @Override
   	public Field wrapFieldWithHelp(Field field, String key) {
   		if (cannotEdit()) return field;
   		return helpFactory.wrapField(field, key);
   	}

    @Override
   	public Component wrapComponentWithHelp(Component component, String key) {
   		if (cannotEdit()) return component;
   		return helpFactory.wrapComponentWithHelp(component, key);
   	}

   	public Component wrapComponentWithHelp(Component component, String key, String iconPlacement, String popupPlacement) {
   		if (cannotEdit()) return component;
   		return helpFactory.wrapComponentWithHelp(component,
                   key,
                   Placement.valueOf(iconPlacement),
                   Placement.valueOf(popupPlacement));
   	}
}
