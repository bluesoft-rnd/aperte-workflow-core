package pl.net.bluesoft.rnd.processtool.ui.jsp;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.pt.utils.lang.LocalizedComparator;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import java.util.*;

import static pl.net.bluesoft.util.lang.Formats.nullifyBlank;
import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 10:32
 */
public class ProcessStartList {
	// used by processStartList.jsp
	public static Map<String, List<ProcessDefinitionConfig>> getGroupedConfigs(List<ProcessDefinitionConfig> availableConfigurations, Locale locale) {
		I18NSource i18NSource = I18NSourceFactory.createI18NSource(locale);
		Map<String, List<ProcessDefinitionConfig>> result = new TreeMap<String, List<ProcessDefinitionConfig>>(getNullLastComparator(locale));

		for (ProcessDefinitionConfig config : availableConfigurations) {
			if (hasText(config.getProcessGroup())) {
				String[] groupNames = config.getProcessGroup().split(",");

				for (String groupName : groupNames) {
					getList(result, groupName, i18NSource).add(config);
				}
			}
			else {
				getList(result, null, i18NSource).add(config);
			}
		}

		for (List<ProcessDefinitionConfig> list : result.values()) {
			sortByLocalizedDescription(list, i18NSource);
		}

		return result;
	}

	public static void sortByLocalizedDescription(List<ProcessDefinitionConfig> list, final I18NSource i18NSource) {
		Collections.sort(list, new LocalizedComparator<ProcessDefinitionConfig>(i18NSource.getLocale()) {
			@Override
			protected String getValue(ProcessDefinitionConfig config) {
				return nvl(i18NSource.getMessage(config.getDescription()));
			}
		});
	}

	private static List<ProcessDefinitionConfig> getList(Map<String, List<ProcessDefinitionConfig>> result, String groupName, I18NSource i18NSource) {
		String localizedGroupName = groupName != null ? i18NSource.getMessage(groupName.trim()) : null;
		List<ProcessDefinitionConfig> list = result.get(localizedGroupName);

		if (list == null) {
			list = new ArrayList<ProcessDefinitionConfig>();
			result.put(localizedGroupName, list);
		}
		return list;
	}

	private static Comparator<String> getNullLastComparator(Locale locale) {
		return new LocalizedComparator<String>(locale) {
			@Override
			public int compare(String s1, String s2) {
				s1 = nullifyBlank(s1);
				s2 = nullifyBlank(s2);
				if (s1 == null && s2 == null) {
					return 0;
				}
				if (s1 == null) {
					return 1;
				}
				if (s2 == null) {
					return -1;
				}
				return super.compare(s1, s2);
			}

			@Override
			protected String getValue(String s) {
				return s;
			}
		};
	}
}
