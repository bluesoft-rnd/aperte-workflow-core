package pl.net.bluesoft.rnd.processtool;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
public interface ISettingsProvider
{
    /** Get current setting value */
    String getSetting(IProcessToolSettings settings);

    void setSetting(IProcessToolSettings settingKey, String value);

}
