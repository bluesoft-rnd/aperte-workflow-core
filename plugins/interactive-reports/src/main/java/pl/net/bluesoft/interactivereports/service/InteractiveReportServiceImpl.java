package pl.net.bluesoft.interactivereports.service;

import pl.net.bluesoft.interactivereports.templates.InteractiveReportTemplate;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.osgi.OSGiBundleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-06-24
 */
public class InteractiveReportServiceImpl implements InteractiveReportService {
	private final Map<String, InteractiveReportTemplate> reportTemplates = new HashMap<String, InteractiveReportTemplate>();

	//TODO potrzeba roznych funkcji zwracajacych dane w formacie json

	@Override
	public void registerReportTemplate(String reportKey, InteractiveReportTemplate reportTemplate) {
		reportTemplates.put(reportKey, reportTemplate);
	}

	@Override
	public void unregisterReportTemplate(String reportKey) {
		reportTemplates.remove(reportKey);
	}

	@Override
	public Map<String, InteractiveReportTemplate> getAvailableReportTemplates(UserData user) {
		return filterByUser(user);
	}

	private Map<String, InteractiveReportTemplate> filterByUser(UserData user) {
		Map<String, InteractiveReportTemplate>  result = new HashMap<String, InteractiveReportTemplate>();

		for (Map.Entry<String, InteractiveReportTemplate> entry : reportTemplates.entrySet()) {
			if (entry.getValue().isAvailable(user)) {
				result.put(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	@Override
	public InteractiveReportTemplate getReportTemplate(String key) {
		return reportTemplates.get(key);
	}
}
