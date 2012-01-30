package org.aperteworkflow.portlets;

import org.aperteworkflow.ui.processadmin.ProcessInstanceAdminManagerPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

/**
 *
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceManagerPortlet extends GenericVaadinPortlet2BpmApplication {
    @Override
    protected void initializePortlet() {
    }

    @Override
    protected void renderPortlet() {
        getMainWindow().setContent(new ProcessInstanceAdminManagerPane(this, bpmSession));
    }
}
