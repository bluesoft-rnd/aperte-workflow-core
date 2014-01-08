package org.aperteworkflow.files;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

import java.util.logging.Logger;

/**
 *
 */
public class Activator implements BundleActivator 
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;

    private final Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception 
    {
    	SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    	

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
