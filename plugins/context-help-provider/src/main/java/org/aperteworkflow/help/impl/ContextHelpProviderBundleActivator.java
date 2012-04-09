package org.aperteworkflow.help.impl;

import com.vaadin.Application;
import org.aperteworkflow.ui.help.HelpProvider;
import org.aperteworkflow.ui.help.HelpProviderFactory;
import org.aperteworkflow.ui.view.ViewRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.util.HashMap;
import java.util.Hashtable;


/**
 * @author tlipski@bluesoft.net.pl
 */
public class ContextHelpProviderBundleActivator implements BundleActivator {


    @Override
    public void start(BundleContext bundleContext) throws Exception {

        bundleContext.registerService(HelpProviderFactory.class.getName(),
                new HelpProviderFactory() {
                    @Override
                    public HelpProvider getInstance(Application application, ProcessDefinitionConfig cfg) {
                        HelpProviderImpl helpProvider = new HelpProviderImpl();
                        helpProvider.prepare(application, cfg);
                        return helpProvider;
                    }
                }, new Hashtable());

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
