package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao;

import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 22:41
 */
public class BpmNotificationTemplateDAO extends SimpleHibernateBean<BpmNotificationTemplate> {
	public BpmNotificationTemplateDAO() {
		super(getThreadProcessToolContext().getHibernateSession());
	}

	public BpmNotificationTemplate getByName(String name) {
		return (BpmNotificationTemplate)getSession().createCriteria(BpmNotificationTemplate.class)
				.add(Restrictions.eq(BpmNotificationTemplate._TEMPLATE_NAME, name))
				.uniqueResult();
	}
}
