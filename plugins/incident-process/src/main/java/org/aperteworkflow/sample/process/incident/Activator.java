package org.aperteworkflow.sample.process.incident;

import org.aperteworkflow.sample.process.incident.services.EmployeeService;
import org.aperteworkflow.sample.process.incident.services.EmployeeServiceMock;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;

/**
 * @author mpawlak@bluesoft.net.pl
 */
public class Activator implements BundleActivator {
	
	@Override
	public void start(BundleContext context) throws Exception {
        injectImplementation();
    }
	
	/** Denpendency Injection */
	private void injectImplementation() {
		/* Inject Liferay based user source */
		ClassDependencyManager.getInstance().injectImplementation(EmployeeService.class, EmployeeServiceMock.class, 1);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}
