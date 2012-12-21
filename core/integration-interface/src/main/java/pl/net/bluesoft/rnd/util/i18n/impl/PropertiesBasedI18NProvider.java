package pl.net.bluesoft.rnd.util.i18n.impl;

import pl.net.bluesoft.rnd.util.i18n.I18NProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class PropertiesBasedI18NProvider implements I18NProvider {

	protected Map<String, Properties> propertiesCache = new HashMap();

	protected Logger logger = Logger.getLogger(PropertiesBasedI18NProvider.class.getName());
	protected PropertyLoader propertyLoader;
	protected String path;

	public PropertiesBasedI18NProvider(PropertyLoader classloader, String path) {
		this.propertyLoader = classloader;
		this.path = path;
	}

	protected synchronized Properties getCachedProperties(String lang) {
		if (propertiesCache.containsKey(lang)) {
			return propertiesCache.get(lang);
		}
		String resourcePath = path + lang + ".properties";
		Properties p = new Properties();
		try {
			logger.log(Level.FINEST, "Attempting to load i18n resource: " + resourcePath);
			InputStream stream = propertyLoader.loadProperty(resourcePath);
			try {
				if (stream == null) {
					logger.log(Level.FINEST, "FAILED to load i18n resource: " + resourcePath + " using " + propertyLoader);
				} else {
					p.load(stream);
					logger.log(Level.FINEST, "Successful load of i18n resource: " + resourcePath + " using " + propertyLoader);
				}
			}
			finally {
				if (stream != null) stream.close();
			}
		} catch (IOException e) {
			logger.log(Level.FINEST, "FAILED to load i18n resource: " + resourcePath + " using " + propertyLoader, e);
		}
		propertiesCache.put(lang, p);

		return p;
	}

	@Override
	public boolean hasFullyLocalizedMessage(String key, Locale locale) {
		if (key == null) return false;
		return getCachedProperties("_" + locale.getLanguage() + "_" + locale.getCountry()).containsKey(key);
	}

	@Override
	public boolean hasLocalizedMessage(String key, Locale locale) {
		if (key == null) return false;
		return getCachedProperties("_" + locale.getLanguage()).containsKey(key);
	}

	@Override
	public String getMessage(String key, Locale locale) {
		if (key == null) return null;
		String msg = getCachedProperties("_" + locale.getLanguage() + "_" + locale.getCountry()).getProperty(key);
		if (msg == null) {
			msg = getCachedProperties("_" + locale.getLanguage()).getProperty(key);
		}
		if (msg == null) {
			msg = getCachedProperties("").getProperty(key);
		}
		return msg;
	}

}
