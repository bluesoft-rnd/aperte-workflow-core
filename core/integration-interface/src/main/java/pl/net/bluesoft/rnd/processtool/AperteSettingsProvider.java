package pl.net.bluesoft.rnd.processtool;

import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;
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
    private final ExpiringCache<String, String> settings = new ExpiringCache<String, String>(60 * 1000);

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
                    return getSettingWithContext(ctx, setting);
                else
                    return processToolRegistry.withProcessToolContext(new ReturningProcessToolContextCallback<String>() {
                        @Override
                        public String processWithContext(ProcessToolContext ctx) {
                            return getSettingWithContext(ctx, setting);
                        }
                    }, ProcessToolContextFactory.ExecutionType.NO_TRANSACTION);
            }
        });
    }

    private String getSettingWithContext(ProcessToolContext ctx, String key)
    {
        ProcessToolSetting setting = (ProcessToolSetting) ctx.getHibernateSession().createCriteria(ProcessToolSetting.class)
                .add(Restrictions.eq("key", key)).uniqueResult();
        return setting != null ? setting.getValue() : null;
    }

    @Override
    public void setSetting(final IProcessToolSettings settingKey, final String value)
    {
        settings.put(settingKey.toString(), value);

        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if(ctx != null)
            setSettingWithContext(ctx, settingKey.toString(), value);
        else
            processToolRegistry.withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    setSettingWithContext(ctx, settingKey.toString(), value);
                }
            });
    }

    @Override
    public void invalidateCache() {
        settings.clear();
    }

    private void setSettingWithContext(ProcessToolContext ctx, String key, String newValue)
    {
        ProcessToolSetting setting = (ProcessToolSetting) ctx.getHibernateSession().createCriteria(ProcessToolSetting.class)
                .add(Restrictions.eq("key", key)).uniqueResult();

        if(setting == null)
        {
            setting = new ProcessToolSetting();
            setting.setKey(key.toString());
        }
        setting.setValue(newValue);
        ctx.getHibernateSession().saveOrUpdate(setting);
    }

}
