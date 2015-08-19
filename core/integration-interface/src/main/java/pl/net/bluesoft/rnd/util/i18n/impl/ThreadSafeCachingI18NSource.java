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

	@Override
	public String getMessage(String key) {
		if (key == null) {
			return null;
		}
		return getCachedMessage(propertyCaches[getIdx(key)], key);
	}

	private int getIdx(String key) {
		return Math.abs(key.hashCode()) % propertyCaches.length;
	}

	private String getCachedMessage(Map<String, String> cachedProperties, String key) {
		synchronized (cachedProperties) {
			String p = cachedProperties.get(key);
			if (p == null) {
				p = i18NSource.getMessage(key);
				handleSearchResult(cachedProperties, key, p);
			}
			return p;
		}
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
		String message = getMessage(key);
		return MessageFormat.format(message, params);
	}

	@Override
	public Locale getLocale() {
		return i18NSource.getLocale();
	}
}
