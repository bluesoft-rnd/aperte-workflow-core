package pl.net.bluesoft.rnd.util.i18n;

import java.util.Locale;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface I18NSource {
	String getMessage(String key);
	String getMessage(String key, Object... params);

	Locale getLocale();

    class ThreadUtil {
        public static final ThreadLocal<I18NSource> i18nSource = new ThreadLocal<I18NSource>();

        public static void setThreadI18nSource(I18NSource source) {
            i18nSource.set(source);
        }

        public static I18NSource getThreadI18nSource() {
            return i18nSource.get();
        }
        
        public static void removeThreadI18nSource() {
            i18nSource.remove();
        }
        
        public static String getLocalizedMessage(String key) {
            return getThreadI18nSource().getMessage(key);
        }
    }
}
