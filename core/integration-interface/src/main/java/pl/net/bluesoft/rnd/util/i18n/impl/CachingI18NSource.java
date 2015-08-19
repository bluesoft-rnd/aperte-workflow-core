package pl.net.bluesoft.rnd.util.i18n.impl;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-07-27
 * Time: 14:15
 */
public class CachingI18NSource implements I18NSource {
	private final I18NSource i18NSource;

	private final Map<String, String> cachedProperties = new HashMap<String, String>();

	public CachingI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
	}

	@Override
	public String getMessage(String key) {
		String p = cachedProperties.get(key);
		if (p == null) {
			p = i18NSource.getMessage(key);
			if (p != null && !p.equals(key)) {
				cachedProperties.put(key, p);
			}
		}
		return p;
	}

	@Override
	public String getMessage(String key, Object... params) {
		String message = getMessage(key);
		return MessageFormat.format(message, params);
	}

	@Override
	public Locale getLocale() {
		return i18NSource.getLocale();
	}
}
