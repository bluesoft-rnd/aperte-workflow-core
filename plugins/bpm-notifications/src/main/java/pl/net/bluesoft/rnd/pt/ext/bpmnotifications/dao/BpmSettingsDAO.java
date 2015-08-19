package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessToolSetting;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * Created by mpawluczuk on 2014-11-17.
 */
public class BpmSettingsDAO extends SimpleHibernateBean<ProcessToolSetting> {
	public BpmSettingsDAO() {
		super(getThreadProcessToolContext().getHibernateSession());
	}
}
