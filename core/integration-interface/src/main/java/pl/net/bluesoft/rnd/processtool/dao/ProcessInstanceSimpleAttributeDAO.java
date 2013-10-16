package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceSimpleAttribute;

import java.util.Collection;
import java.util.Map;

/**
 *@author kkolodziej@bluesoft.net.pl
 */
public interface ProcessInstanceSimpleAttributeDAO extends HibernateBean<ProcessInstanceSimpleAttribute> {
	String getSimpleAttributeValue(Long processId, String key);

	void setSimpleAttribute(Long processId, String key, String newValue);

	Map<String, String> getSimpleAttributesList(Long processId);
	Map<String,String> getSimpleAttributeValues(Long processId, Collection<String> keys);
}
