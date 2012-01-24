package pl.net.bluesoft.rnd.processtool.portlets.plugins;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.plugins.PluginManager;
import pl.net.bluesoft.rnd.processtool.ui.plugins.PluginsManagerPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.net.bluesoft.rnd.util.vaadin.VaadinUtility.*;

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
