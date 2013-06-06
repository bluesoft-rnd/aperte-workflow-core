package pl.net.bluesoft.rnd.pt.ext.emailcapture;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.emailcapture.model.EmailCheckerConfiguration;
import pl.net.bluesoft.rnd.pt.ext.emailcapture.model.EmailCheckerRuleConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class Activator implements BundleActivator {

	boolean run = true;

	private final Logger logger = Logger.getLogger(Activator.class.getName());

	@Override
	public void start(final BundleContext context) throws Exception {

		final ProcessToolRegistry toolRegistry = getRegistry(context);
		toolRegistry.registerModelExtension(EmailCheckerConfiguration.class);
		toolRegistry.registerModelExtension(EmailCheckerRuleConfiguration.class);
		toolRegistry.commitModelExtensions();
		new Thread(new Runnable() {

			@Override
			public void run() {
				run = true;
				while (run) {
					try {
						Thread.sleep(10000);
						try {
							toolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
								@Override
								public void withContext(ProcessToolContext ctx) 
								{
									new EmailChecker(ctx).run();
								}
							});
						}
						catch (Exception e) {
							logger.log(Level.SEVERE, e.getMessage(), e);	
						}
					} catch (InterruptedException e) {
						logger.log(Level.INFO, e.getMessage(), e);
					}

				}
			}
		}).start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		run = false;
	}

	private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}
}
