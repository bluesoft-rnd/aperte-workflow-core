package org.aperteworkflow.portlets;

import org.aperteworkflow.ui.processmanager.ProcessDefinitionManagerPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessConsolePortlet extends GenericVaadinPortlet2BpmApplication {
    @Override
        protected void initializePortlet() {
        }

        @Override
        protected void renderPortlet() {
            getMainWindow().setContent(new ProcessDefinitionManagerPane(this));
        }
}
