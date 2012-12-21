package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 22:51:25
 */
public interface CustomValueProvider<EntryType> {
	String getValue(String key, DictEntryProvider entryProvider, I18NSource i18NSource);
}
