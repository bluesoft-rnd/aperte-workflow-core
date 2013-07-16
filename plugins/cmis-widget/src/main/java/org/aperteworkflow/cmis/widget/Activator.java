package org.aperteworkflow.cmis.widget;

import java.util.Hashtable;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

public class Activator implements BundleActivator 
{
	@Override
	public void start(BundleContext context) throws Exception 
	{
		ProcessToolRegistry registry = getRegistry(context);

		for(Bundle bundle: context.getBundles())
		{
			if(bundle.getSymbolicName().equals("org.aperteworkflow.cmis"))
			{
				ServiceReference reference = bundle.getBundleContext().getServiceReference(SessionFactory.class.getName());
				SessionFactory sessionFactory = (SessionFactory)bundle.getBundleContext().getService(reference);

		        registry.registerService(SessionFactory.class, sessionFactory, new Properties());
		        context.registerService(SessionFactory.class.getName(), sessionFactory, new Hashtable<String, String>());
			}
		}


	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}
	
	private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}

}
