package pl.net.bluesoft.rnd.processtool.portlets.plugins;

import pl.net.bluesoft.rnd.processtool.ui.plugins.PluginsManagerPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

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
