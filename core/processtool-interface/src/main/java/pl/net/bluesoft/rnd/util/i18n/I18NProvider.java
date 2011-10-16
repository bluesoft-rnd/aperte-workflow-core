package pl.net.bluesoft.rnd.util.i18n;

import java.util.Locale;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface I18NProvider {
	boolean hasFullyLocalizedMessage(String key, Locale locale);
	boolean hasLocalizedMessage(String key, Locale locale);
	String getMessage(String key, Locale locale);
}
