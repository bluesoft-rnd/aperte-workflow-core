package pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.dao;

import java.util.List;

import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.pt.ext.apertereportsintegration.model.ReportTemplate;

public class ReportTemplateDAO extends SimpleHibernateBean<ReportTemplate> {
	public ReportTemplateDAO() {
		super(ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession());
	}

	public ReportTemplate getReportTemplateByName(String reportTemplateName) throws AperteReportsRuntimeException {

		ReportTemplate reportTemplate = (ReportTemplate)session.createCriteria(ReportTemplate.class)
				.add(Restrictions.eq("reportname", reportTemplateName)).uniqueResult();
		
		if(reportTemplate == null)
			throw new AperteReportsRuntimeException("Report template not found: "+reportTemplateName, ErrorCodes.INVALID_REPORT_TYPE);

		reportTemplate.setContent(reportTemplate.getContent());

		return reportTemplate;
	}
	
	@SuppressWarnings("unchecked")
	public List<ReportTemplate> loadAll(){
		return session.createCriteria(ReportTemplate.class).list();
	}
}
