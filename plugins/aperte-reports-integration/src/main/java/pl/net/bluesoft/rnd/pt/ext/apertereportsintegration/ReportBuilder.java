package pl.net.bluesoft.rnd.pt.ext.apertereportsintegration;

import org.apertereports.engine.EmptySubreportProvider;
import org.apertereports.engine.ReportMaster;
import pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.model.ReportTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2012-09-27
 * Time: 19:46
 */
public class ReportBuilder {
	private String reportName;
	private Map<String, Object> parameters = new HashMap<String,Object>();
	private String format = "PDF";

	public ReportBuilder(String reportName) {
		this.reportName = reportName;
	}

	public ReportBuilder addParameter(String key, Object value) {
		if(value != null) {
			parameters.put(key, value);
		}
		return this;
	}

	public ReportBuilder setFormat(String format) {
		this.format = format;
		return this;
	}

	public byte[] getReportBytes() {
		try {
			ReportTemplateDAO dao = new ReportTemplateDAO();
			ReportTemplate reportTemplate = dao.getReportTemplateByName(reportName);

			if (reportTemplate == null) {
				Logger.getLogger(ReportBuilder.class.getSimpleName()).severe("No report found with key " + reportName);
				return null;
			}

			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

			try {
				ReportMaster reportMaster = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(),
						new EmptySubreportProvider());
				Map<String, String> configuration = new HashMap<String,String>();
				byte[] data = reportMaster.generateAndExportReport(format, parameters, configuration);

				return data;
			}
			finally {
				Thread.currentThread().setContextClassLoader(contextClassLoader);
			}

		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
