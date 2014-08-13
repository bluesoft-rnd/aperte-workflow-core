package pl.net.bluesoft.rnd.processtool;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public interface ISettingsProvider
{
    /** Get current setting value */
    String getSetting(IProcessToolSettings settings);

    /** Get current setting value */
    String getSetting(String key);

    void setSetting(IProcessToolSettings settingKey, String value);

}
