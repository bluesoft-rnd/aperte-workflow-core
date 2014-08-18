package pl.net.bluesoft.interactivereports.service;

import pl.net.bluesoft.interactivereports.templates.InteractiveReportTemplate;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-06-26
 */
public interface InteractiveReportService {
	void registerReportTemplate(String reportKey, InteractiveReportTemplate reportTemplate);
	void unregisterReportTemplate(String reportKey);
	Map<String, InteractiveReportTemplate> getAvailableReportTemplates(UserData user);
	InteractiveReportTemplate getReportTemplate(String key);
}
