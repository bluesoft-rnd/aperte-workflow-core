package org.aperteworkflow.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * This abstract class should be used when new 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class ProcessToolContextThread implements Runnable 
{
	/** Run new thread with existing context */
	public abstract void runWithContext(ProcessToolContext ctx);


	@Override
	public final void run() 
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		
		/* Context exists, no need to create new one */
		if(ctx != null)
		{
			runWithContext(ctx);
		}
		/* There is no context in parent thread, create new one */
		else
		{
			getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
				@Override
				public void withContext(ProcessToolContext ctx) {
					runWithContext(ctx);
				}
			});
		}
	}
}
