package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 22:41
 */
public class BpmNotificationMailPropertiesDAO extends SimpleHibernateBean<BpmNotificationMailProperties> {
	public BpmNotificationMailPropertiesDAO() {
		super(getThreadProcessToolContext().getHibernateSession());
	}
}
