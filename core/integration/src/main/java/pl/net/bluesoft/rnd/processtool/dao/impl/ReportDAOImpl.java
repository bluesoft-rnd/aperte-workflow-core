package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ReportDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.report.Report;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportDefinition;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportParam;
import pl.net.bluesoft.rnd.processtool.model.report.xml.ReportParamType;
import pl.net.bluesoft.rnd.pt.utils.xml.OXHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;
import static pl.net.bluesoft.rnd.processtool.model.report.Report._DESCRIPTION;
import static pl.net.bluesoft.rnd.processtool.model.report.Report._REPORT_ID;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:41
 */
public class ReportDAOImpl extends SimpleHibernateBean<Report> implements ReportDAO {
	public ReportDAOImpl(Session session) {
		super(session);
	}

	@Override
	public Map<String, String> getReportIds() {
		List<Object[]> list = session.createCriteria(Report.class)
				.setProjection(projectionList()
						.add(property(_REPORT_ID))
						.add(property(_DESCRIPTION)))
				.list();

		Map<String, String> result = new HashMap<String, String>();

		for (Object[] row : list) {
			String reportId = (String)row[0];
			String description = (String)row[1];

			result.put(reportId, description);
		}
		return result;
	}

	@Override
	public ReportDefinition getReportDefinition(String reportId) {
		Report report = (Report)session.createCriteria(Report.class)
				.add(Restrictions.eq(_REPORT_ID, reportId))
				.uniqueResult();

		if (report != null) {
			return fromXml(report.getDefinition());
		}
		return null;
	}

	private static ReportDefinition fromXml(String xml) {
		if (xml == null) {
			return null;
		}

		return (ReportDefinition)XmlSerializer.INSTANCE.unmarshall(xml);
	}

	private static class XmlSerializer extends OXHelper {
		public static final XmlSerializer INSTANCE = new XmlSerializer();

		@Override
		protected Class[] getSupportedClasses() {
			return new Class[] {
				ReportDefinition.class,
				ReportParam.class,
				ReportParamType.class
			};
		}
	}
}
