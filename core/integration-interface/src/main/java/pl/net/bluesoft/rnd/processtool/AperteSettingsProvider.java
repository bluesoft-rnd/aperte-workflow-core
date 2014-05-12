package pl.net.bluesoft.rnd.processtool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.lang.ExpiringCache;

/**
 *
 * Aperte settings provider
 *
 * @author: mpawlak@bluesoft.net.pl
 */
@Component
public class AperteSettingsProvider implements ISettingsProvider
{
    private static final ExpiringCache<String, String> settings = new ExpiringCache<String, String>(60 * 1000);

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    @Override
    public String getSetting(IProcessToolSettings settingKey) {
        return getSetting(settingKey.toString());
    }

    @Override
    public String getSetting(String key)
    {
        return settings.get(key, new ExpiringCache.NewValueCallback<String, String>() {
            @Override
            public String getNewValue(final String setting)
            {
                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                if(ctx != null)
                    return ctx.getSetting(setting);
                else
                    return processToolRegistry.withProcessToolContext(new ReturningProcessToolContextCallback<String>() {
                        @Override
                        public String processWithContext(ProcessToolContext ctx) {
                            return ctx.getSetting(setting);
                        }
                    });
            }
        });
    }

    @Override
    public void setSetting(final IProcessToolSettings settingKey, final String value)
    {
        settings.put(settingKey.toString(), value);

        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if(ctx != null)
            ctx.setSetting(settingKey, value);
        else
            processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    ctx.setSetting(settingKey, value);
                }
            });
    }

}
