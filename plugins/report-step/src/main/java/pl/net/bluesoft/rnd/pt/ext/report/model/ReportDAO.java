package pl.net.bluesoft.rnd.pt.ext.report.model;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;

import java.util.List;

public class ReportDAO {

	protected Session session;

    public ReportDAO(Session session) {
        this.session = session;
    }

    public ReportDAO() {
		session = ProcessToolContext.Util.getThreadProcessToolContext()
				.getHibernateSession();
	}

	@SuppressWarnings("unchecked")
	public List<ReportTemplate> loadAll(){
		return session.createCriteria(ReportTemplate.class).list();
	}

	public ReportTemplate loadByName(String reportName){
		return (ReportTemplate) session.createCriteria(ReportTemplate.class).add(Restrictions.eq("reportname", reportName)).uniqueResult();
	}

}
