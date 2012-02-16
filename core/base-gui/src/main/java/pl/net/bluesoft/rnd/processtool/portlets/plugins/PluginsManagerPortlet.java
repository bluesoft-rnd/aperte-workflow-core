package pl.net.bluesoft.rnd.processtool.portlets.plugins;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.plugins.PluginsManagerPane;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class PluginsManagerPortlet extends GenericVaadinPortlet2BpmApplication {
    @Override
    protected void initializePortlet() {
    }

    @Override
    protected void renderPortlet() {
        getMainWindow().setContent(new PluginsManagerPane(this));
    }

}
