package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.dao.ProcessStateActionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;

 
import java.util.*;
import java.util.logging.Logger;

import static org.hibernate.criterion.Restrictions.eq;

/**
 * @author kkolodziej@bluesoft.net.pl
 */
public class ProcessStateActionDAOImpl extends SimpleHibernateBean<ProcessStateAction> implements ProcessStateActionDAO {
	 private static Logger logger = Logger.getLogger(ProcessStateActionDAOImpl.class.getName());
    public ProcessStateActionDAOImpl(Session session) {   
          super(session);
      }

	@Override
	public List<ProcessStateAction> getActionsListByDefinition(
			ProcessDefinitionConfig processDefinitionConfig) {
		
		long start = System.currentTimeMillis();
			List actionlist = session.createCriteria(ProcessStateAction.class)
				.createAlias("config", "conf")
				.createAlias("conf.definition", "pIdef")
				.add(eq("pIdef.id", processDefinitionConfig.getId()))
		.addOrder(Order.desc("id"))
.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list(); 
			
			long duration = System.currentTimeMillis() - start;
			logger.severe("getActionsListByDefinition: " +  duration);
			
			
			return actionlist;
	} 
	
	@Override
	public List<ProcessStateAction> getActionByNameFromDefinition(ProcessDefinitionConfig processDefinitionConfig, String bpmName){
		ArrayList<ProcessStateAction> processStateActionList = new ArrayList<ProcessStateAction>();
		List<ProcessStateAction> actionsListByDefinition = getActionsListByDefinition(processDefinitionConfig);
		for (ProcessStateAction processStateAction : actionsListByDefinition) {
			String procesStateBpmName = processStateAction.getBpmName();
			if(procesStateBpmName.equals(bpmName)){
				processStateActionList.add(processStateAction);
			}
		}
		return processStateActionList;
		
		
	}
	
	@Override
	public List<ProcessStateAction> getActionsBasedOnStateAndDefinitionId(String state,Long definitionId) {
		 long start = System.currentTimeMillis(); 
		
			List actionList = session.createCriteria(ProcessStateAction.class)
				.createAlias("config", "conf")
				.createAlias("conf.definition", "pIdef")
				.add(eq("pIdef.id", definitionId))
				.add(eq("conf.name",state))
		.addOrder(Order.desc("id"))
.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list(); 
			 long duration = System.currentTimeMillis() - start;
				logger.severe("getActionsBasedOnStateAndDefinitionId: " +  duration);
			return actionList;
	} 
	
	
	


}
  