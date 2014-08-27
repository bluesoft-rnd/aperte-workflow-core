package pl.net.bluesoft.interactivereports.util;

import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * User: POlszewski
 * Date: 2014-07-11
 */
public class ReportUtil {
	public static Date getDate(Object value) {
		if (value != null) {
			try {
				return new SimpleDateFormat("dd-MM-yyyy").parse((String)value);
			}
			catch (ParseException e) {

				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public static String getDictVal(String itemKey, String dictName, I18NSource messageSource) {
		return getThreadProcessToolContext()
				.getProcessDictionaryDAO()
				.fetchDictionary(dictName)
				.lookup(itemKey)
				.getValueForCurrentDate()
				.getValue(messageSource.getLocale());
	}

	private ReportUtil() {}
}
