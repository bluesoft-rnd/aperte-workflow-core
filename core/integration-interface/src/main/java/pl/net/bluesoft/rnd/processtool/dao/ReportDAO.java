package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.report.Report;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportDefinition;

import java.util.Map;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:40
 */
public interface ReportDAO extends HibernateBean<Report> {
	Map<String, String> getReportIds();
	ReportDefinition getReportDefinition(String reportId);
}
