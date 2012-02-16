package org.aperteworkflow.portlets;

import org.aperteworkflow.ui.processmanager.ProcessDefinitionManagerPane;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDefinitionManagerPortlet extends GenericVaadinPortlet2BpmApplication {
    @Override
    protected void initializePortlet() {
    }

    @Override
    protected void renderPortlet() {
        getMainWindow().setContent(new ProcessDefinitionManagerPane(this));
    }
}
