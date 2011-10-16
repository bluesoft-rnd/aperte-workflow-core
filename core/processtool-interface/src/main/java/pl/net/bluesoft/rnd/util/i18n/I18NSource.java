package pl.net.bluesoft.rnd.util.i18n;

import java.util.Locale;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface I18NSource {
	String getMessage(String key);
	String getMessage(String key, String defaultValue);
	Locale getLocale();
	void setLocale(Locale locale);

}
