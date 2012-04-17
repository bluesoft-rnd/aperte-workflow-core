package org.aperteworkflow.util;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.plugins.RegistryHolder;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ContextUtil {
    public static <T> T withContext(final ReturningProcessToolContextCallback<T> callback) {
            return RegistryHolder.getRegistry().withExistingOrNewContext(new ReturningProcessToolContextCallback<T>() {
                @Override
                public T processWithContext(ProcessToolContext ctx) {
                    ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                    try {
                        return callback.processWithContext(ctx);
                    }
                    finally {
                        ProcessToolContext.Util.removeThreadProcessToolContext();
                    }
                }
            });
        }
}
