package org.aperteworkflow.ui.help;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface HelpProvider {

    static final String PLACEMENT_RIGHT = "RIGHT";
    static final String PLACEMENT_LEFT = "LEFT";
    static final String PLACEMENT_ABOVE = "ABOVE";
    static final String PLACEMENT_BELOW = "BELOW";
    static final String PLACEMENT_DEFAULT = "DEFAULT";

    Component helpIcon(String taskName, String s);

    Component getHelpIcon(String key);

    Component getHelpIcon(String key, String message);

    Field wrapFieldWithHelp(Field field, String key);

    void prepare(Application application, ProcessDefinitionConfig cfg, boolean canEdit, String helpDictionaryName);

    Component wrapComponentWithHelp(Component component, String key);
    Component wrapComponentWithHelp(Component component, String key, String iconPlacement, String popupPlacement);


    void attachToLayout(Layout layout);
    Field getFieldWithHelp(final Field wrappedField, Component helpButton);
    void makeTableHelpEnabled(Table t);
    void addHelpForColumn(Table t, Object propertyId, String key);
    boolean isFieldWithHelp(Field f);

    void makeTableHelpEnabled(Table t, Component helpPosition);
    void showHelpFor(Component component);
}
