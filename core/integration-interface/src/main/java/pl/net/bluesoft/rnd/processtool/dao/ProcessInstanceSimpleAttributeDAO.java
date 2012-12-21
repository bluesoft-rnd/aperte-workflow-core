package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.Date;
import java.util.List;

/**
 *@author kkolodziej@bluesoft.net.pl
 */
public interface ProcessInstanceSimpleAttributeDAO extends HibernateBean<ProcessInstanceSimpleAttribute> {

	String getSimpleAttributeValue(String key, ProcessInstance processInstance);

	ProcessInstanceSimpleAttribute setSimpleAttribute(String key,
			String newValue, ProcessInstance processInstance);

	List<ProcessInstanceSimpleAttribute> getSimpleAttributesList(
			ProcessInstance processInstance);
  
}
