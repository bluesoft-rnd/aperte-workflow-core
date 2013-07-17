package pl.net.bluesoft.rnd.processtool.facade;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.token.callbacks.GetSettingCallback;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Abstract facade which encapsultes the callback processing logic
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public abstract class AbstractFacade 
{
	/** Process callback. If there is no context, new is created */
	protected <T> T processCallback(ReturningProcessToolContextCallback<T> callback)
	{
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		if(ctx == null || !ctx.isActive())
		{
			return getRegistry().withProcessToolContext(callback);
		}
		else
		{
			return callback.processWithContext(ctx);
		}
	}
	
	/** Get current context or generate new one if not available */
	protected String getSetting(IProcessToolSettings settingKey)
	{
		GetSettingCallback getSettingCallback = new GetSettingCallback(settingKey);
		
		return processCallback(getSettingCallback);
	}
}
