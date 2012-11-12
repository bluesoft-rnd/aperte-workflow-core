package pl.net.bluesoft.rnd.util.i18n.impl;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-07-27
 * Time: 14:15
 */
public class ThreadSafeCachingI18NSource implements I18NSource {
	private final I18NSource i18NSource;

	private final Map<String, String>[] propertyCaches;

	public ThreadSafeCachingI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
		this.propertyCaches = new Map[499];
		for (int i = 0; i < propertyCaches.length; ++i) {
			propertyCaches[i] = new HashMap<String, String>();
		}
	}

	public String getMessage(String key) {
		if (key == null) {
			return null;
		}
		return getCachedMessage(propertyCaches[getIdx(key)], key);
	}

	public String getMessage(String key, String defaultValue) {
		if (key == null) {
			return defaultValue;
		}
		return getCachedMessage(propertyCaches[getIdx(key)], key, defaultValue);
	}

	private int getIdx(String key) {
		return (int)(Math.abs((long)key.hashCode()) % propertyCaches.length);
	}

	private synchronized String getCachedMessage(Map<String, String> cachedProperties, String key) {
		String p = cachedProperties.get(key);
		if (p == null) {
			p = i18NSource.getMessage(key);
			handleSearchResult(cachedProperties, key, p);
		}
		return p;
	}

	private synchronized String getCachedMessage(Map<String, String> cachedProperties, String key, String defaultValue) {
		String p = cachedProperties.get(key);
		if (p == null) {
			p = i18NSource.getMessage(key, defaultValue);
			handleSearchResult(cachedProperties, key, p);
		}
		return p;
	}

	private void handleSearchResult(Map<String, String> cachedProperties, String key, String p) {
		if (p != null && !p.equals(key)) {
			cachedProperties.put(key, p);
		}
		else {
			I18NSourceFactory.fireMissingKey(this, key);
		}
	}

	@Override
	public String getMessage(String key, Object... params) {
		return getMessage(key, key, params);
	}

	@Override
	public String getMessage(String key, String defaultValue, Object... params) {
		String message = getMessage(key, defaultValue);
		return MessageFormat.format(message, params);
	}

	public Locale getLocale() {
		return i18NSource.getLocale();
	}

	public void setLocale(Locale locale) {
		throw new UnsupportedOperationException();
	}
}
