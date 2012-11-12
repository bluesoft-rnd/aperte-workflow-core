package pl.net.bluesoft.rnd.util.i18n;

import pl.net.bluesoft.rnd.util.i18n.impl.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2012-07-27
 * Time: 13:51
 */
public class I18NSourceFactory {
	private static final Map<Locale, I18NSource> i18NSources = new HashMap<Locale, I18NSource>();

	private static final Set<MissingKeyListener> missingKeyListeners = new HashSet<MissingKeyListener>();

	public interface MissingKeyListener {
		void missingKey(I18NSource i18NSource, String key);
	}

	public static synchronized I18NSource createI18NSource(Locale locale) {
		I18NSource i18NSource = i18NSources.get(locale);
		if (i18NSource == null) {
			i18NSource = new ThreadSafeCachingI18NSource(
					new DefaultI18NSource(locale));
			i18NSources.put(locale, i18NSource);
		}
		return new CachingI18NSource(i18NSource);
	}

	public static synchronized void invalidateCache() {
		i18NSources.clear();
	}

	public static synchronized void addListener(MissingKeyListener listener) {
		missingKeyListeners.add(listener);
	}

	public static synchronized void removeListener(MissingKeyListener listener) {
		missingKeyListeners.remove(listener);
	}

	public static synchronized void fireMissingKey(I18NSource i18NSource, String key) {
		for (MissingKeyListener listener : missingKeyListeners) {
			listener.missingKey(i18NSource, key);
		}
	}

//	static {
//		addListener(new MissingKeyListener() {
//			@Override
//			public void missingKey(I18NSource i18NSource, String key) {
//				Logger.getLogger("I18N").severe("Missing key " + key);
//			}
//		});
//	}
}
