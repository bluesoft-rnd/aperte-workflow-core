package org.aperteworkflow.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ContextUtil {
    public static <T> T withContext(final ReturningProcessToolContextCallback<T> callback) 
    {
		return getRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<T>() {
			@Override
			public T processWithContext(ProcessToolContext ctx) {
				return callback.processWithContext(ctx);
			}
		});
	}
}
