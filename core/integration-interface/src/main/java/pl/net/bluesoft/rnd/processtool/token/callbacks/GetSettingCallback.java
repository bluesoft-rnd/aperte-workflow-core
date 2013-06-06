package pl.net.bluesoft.rnd.processtool.token.callbacks;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;

/**
 * Get current setting value with given key
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class GetSettingCallback implements ReturningProcessToolContextCallback<String>
{
	private IProcessToolSettings key;
	
	public GetSettingCallback(IProcessToolSettings key)
	{
		this.key = key;
	}

	@Override
	public String processWithContext(ProcessToolContext ctx) 
	{
          return ctx.getSetting(key);
	}
	
}