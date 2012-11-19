package pl.net.bluesoft.rnd.processtool.dao.impl;

import static org.hibernate.criterion.Restrictions.eq;

import org.hibernate.Criteria;
import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceSimpleAttributeDAO;
import pl.net.bluesoft.rnd.processtool.dao.UserSubstitutionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;

import java.util.Date;
import java.util.List;

/**
 *@author kkolodziej@bluesoft.net.pl
 */
public class ProcessInstanceSimpleAttributeDAOImpl extends SimpleHibernateBean<ProcessInstanceSimpleAttribute> implements ProcessInstanceSimpleAttributeDAO {
    public ProcessInstanceSimpleAttributeDAOImpl(Session hibernateSession) {
        super(hibernateSession);
    }

    @Override 
	public String getSimpleAttributeValue(String key,ProcessInstance processInstance){
    	
    	long start = System.currentTimeMillis();
    	ProcessInstanceSimpleAttribute pisa = getSimpleAttribute(key,processInstance);
    	 long duration = System.currentTimeMillis() - start;
			logger.severe("getSimpleAttributeValue: " +  duration);
    	
		return pisa.getValue();
	}
    
    @Override 
   	public List<ProcessInstanceSimpleAttribute> getSimpleAttributesList(ProcessInstance processInstance){
    	 long start = System.currentTimeMillis();
    	  List list = session.createCriteria(ProcessInstanceSimpleAttribute.class)
    			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
				.add(eq("processInstance",processInstance)).list();
    	  long duration = System.currentTimeMillis() - start;
			logger.severe("getSimpleAttributesList: " +  duration);
    	  
    	  return list;
   	}
	
	@Override 
	public ProcessInstanceSimpleAttribute setSimpleAttribute(String key, String newValue, ProcessInstance processInstance){
		ProcessInstanceSimpleAttribute pisa = getSimpleAttribute(key,processInstance);
	if(pisa==null){
		return null;	
	}
	pisa.setValue(newValue);
	session.update(pisa);
	return pisa;
	}
	
	private ProcessInstanceSimpleAttribute  getSimpleAttribute(String key,ProcessInstance processInstance){
		return (ProcessInstanceSimpleAttribute) session.createCriteria(ProcessInstanceSimpleAttribute.class)
				.add(eq("processInstance",processInstance))
					.add(eq("key",key)).uniqueResult();
		
		
	}
	
}
