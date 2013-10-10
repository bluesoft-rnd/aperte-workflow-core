package pl.net.bluesoft.rnd.processtool.ui.jsp;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.util.List;
import java.util.Locale;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 11:10
 */
public class SearchView {
	// used by searchView.jsp
	public static void sortByLocalizedDescription(List<ProcessDefinitionConfig> list, Locale locale) {
		ProcessStartList.sortByLocalizedDescription(list, I18NSourceFactory.createI18NSource(locale));
	}
}
