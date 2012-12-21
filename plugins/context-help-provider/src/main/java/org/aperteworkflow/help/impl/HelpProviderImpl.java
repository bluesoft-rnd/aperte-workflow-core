package org.aperteworkflow.help.impl;

import com.vaadin.Application;
import com.vaadin.ui.*;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import org.aperteworkflow.ui.help.HelpProvider;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import org.vaadin.jonatan.contexthelp.Placement;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class HelpProviderImpl implements HelpProvider {

    private HelpFactory helpFactory;
    private boolean canEdit;

    @Override
    public void prepare(Application application, List<ProcessDefinitionConfig> cfgs, boolean canEdit, String helpDictionaryName) {
        ContextHelp contextHelp = new ContextHelp();
        this.canEdit = canEdit;
        application.getMainWindow().getContent().addComponent(contextHelp);
        helpFactory = new HelpFactory(
				cfgs,
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

    @Override
    public void attachToLayout(Layout layout) {
        layout.addComponent(helpFactory.getContextHelp());

    }

    @Override
    public void showHelpFor(Component component) {
        helpFactory.getContextHelp().showHelpFor(component);
    }

    @Override
    public Field getFieldWithHelp(final Field wrappedField, Component helpButton) {
        return new FieldWithHelp(wrappedField, helpButton);
    }

    @Override
    public void makeTableHelpEnabled(Table t) {
        makeTableHelpEnabled(t, t);
    }

    @Override
    public void makeTableHelpEnabled(final Table t, final Component helpPosition) {
        t.setSortDisabled(true);
        t.setData(new HashMap<Object,String>());

        helpFactory.getContextHelp().addHelpForComponent(helpPosition, "help.empty", Placement.ABOVE);
        t.addListener(new Table.HeaderClickListener() {

            @Override
            public void headerClick(Table.HeaderClickEvent event) {
                Map<Object, String> helpMap = (Map<Object, String>) t.getData();
                Object propertyId = event.getPropertyId();
                if (helpMap.containsKey(propertyId)) {
                    helpFactory.showHelp(helpPosition, helpMap.get(propertyId), Placement.ABOVE);
                }
            }
        });
    }

    @Override
    public void addHelpForColumn(Table t, Object propertyId, String key) {
        Map<Object, String> helpMap = (Map<Object, String>) t.getData();
        t.setColumnIcon(propertyId, helpFactory.helpIcon(8));
        helpFactory.logHelpKey(key);
        helpMap.put(propertyId, key);
    }

    @Override
    public Field stripFieldFromHelp(Field f) {
        if (isFieldWithHelp(f)) {
            return Lang2.assumeType(f, FieldWithHelp.class).getField();
        } else {
            return f;
        }
    }

    public boolean isFieldWithHelp(Field f) {
        return f instanceof FieldWithHelp;
    }
}
