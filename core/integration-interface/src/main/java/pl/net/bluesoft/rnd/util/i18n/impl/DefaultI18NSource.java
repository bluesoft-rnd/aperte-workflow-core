package pl.net.bluesoft.rnd.util.i18n.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Locale;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class DefaultI18NSource implements I18NSource 
{
	@Autowired
	private ProcessToolRegistry processToolRegistry;

	private Locale locale;

    public DefaultI18NSource(Locale locale) {
        this.locale = locale;
    }

    @Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public String getMessage(String key)
	{
		if(processToolRegistry == null) {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		}

		Collection<I18NProvider> i18NProviders = processToolRegistry.getBundleRegistry().getI18NProviders();
		//1st run - full localization e.g. _pl_PL
		for (I18NProvider i18NProvider : i18NProviders) {
			if (!i18NProvider.hasFullyLocalizedMessage(key, locale)) continue;
			String m = i18NProvider.getMessage(key, locale);
			if (m != null) return m;
		}

		//2nd run - only country e.g. _pl
		for (I18NProvider i18NProvider : i18NProviders) {
			if (!i18NProvider.hasLocalizedMessage(key, locale)) continue;
			String m = i18NProvider.getMessage(key, locale);
			if (m != null) return m;
		}

		//3rd run - with default values
		for (I18NProvider i18NProvider : i18NProviders) {
			String m = i18NProvider.getMessage(key, locale);
			if (m != null) return m;
		}
		return key;
	}

    @Override
    public String getMessage(String key, Object... params) {
		String message = getMessage(key);
		return MessageFormat.format(message, params);
    }
}
