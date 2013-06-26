package pl.net.bluesoft.rnd.processtool.dao.impl;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.IPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateActionAttribute;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateActionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetAttribute;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidgetPermission;
import pl.net.bluesoft.util.lang.Lang;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDefinitionDAOImpl extends SimpleHibernateBean<ProcessDefinitionConfig>
        implements ProcessDefinitionDAO {

	private Logger logger = Logger.getLogger(ProcessDefinitionDAOImpl.class.getName());

	public ProcessDefinitionDAOImpl(Session session) {
		super(session);
	}

	@SuppressWarnings("unchecked")
	public Collection<ProcessDefinitionConfig> getAllConfigurations() {
		return getSession().createCriteria(ProcessDefinitionConfig.class).addOrder(Order.desc("processName")).list();
	}

	@SuppressWarnings("unchecked")
	public Collection<ProcessDefinitionConfig> getActiveConfigurations() {		
		 long start = System.currentTimeMillis(); 
		
		List<ProcessDefinitionConfig> list = getSession().createCriteria(ProcessDefinitionConfig.class)
				.addOrder(Order.desc("processName"))
				.add(Restrictions.eq("latest", Boolean.TRUE))
				.add(Restrictions.or(Restrictions.eq("enabled", Boolean.TRUE), Restrictions.isNull("enabled")))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
		 
		 
		 long duration = System.currentTimeMillis() - start;
			logger.severe("getActiveConfigurations: " +  duration);
		 return list;
	}

	@Override
	public ProcessDefinitionConfig getActiveConfigurationByKey(String key) {
		return (ProcessDefinitionConfig) getSession().createCriteria(ProcessDefinitionConfig.class)
				.add(Restrictions.eq("latest", Boolean.TRUE))
				.add(Restrictions.eq("bpmDefinitionKey", key)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<ProcessQueueConfig> getQueueConfigs() {
		return getSession().createCriteria(ProcessQueueConfig.class).list();
	}

	@SuppressWarnings("unchecked")
	public ProcessStateConfiguration getProcessStateConfiguration(BpmTask task) {
//		HibernateTemplate ht = getHibernateTemplate();
        List<ProcessStateConfiguration> res = getSession().createCriteria(ProcessStateConfiguration.class)
				.add(Restrictions.eq("definition", task.getProcessInstance().getDefinition()))
				.add(Restrictions.eq("name", task.getTaskName())).list();
		if (res.isEmpty())
			return null;
		return (ProcessStateConfiguration) res.get(0);

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg) {
		cfg.setCreateDate(new Date());
		cfg.setLatest(true);
		Set<ProcessStateConfiguration> stateConfigurations = cfg.getStates();

		for (ProcessStateConfiguration state : stateConfigurations) {
			state.setDefinition(cfg);
			Set<ProcessStateAction> actions = state.getActions();
			for (ProcessStateAction action : actions) {
				action.setConfig(state);
				for (ProcessStateActionPermission p : action.getPermissions()) {
					p.setAction(action);
				}
			}
			cleanupWidgetsTree(state.getWidgets(), null, new HashSet<ProcessStateWidget>());
		}

        for (ProcessDefinitionPermission permission : cfg.getPermissions()) {
            permission.setDefinition(cfg);
        }
        Session session = getSession();
		List<ProcessDefinitionConfig> lst = session.createCriteria(ProcessDefinitionConfig.class)
						.add(Restrictions.eq("latest", true))
						.add(Restrictions.eq("bpmDefinitionKey", cfg.getBpmDefinitionKey())).list();
		for (ProcessDefinitionConfig c : lst) {
			//porównujemy z nową konfiguracją - jeśli nic się nie zmienia, nie wgrywamy wersji
			if (compareDefinitions(cfg,c) && compareDefinitions(c,cfg)) {
				logger.warning("New process definition config is the same as: " + c.getId() + ", therefore skipping DB update");
				return;
			}
			c.setLatest(false);
			session.saveOrUpdate(c);
		}
		
		session.saveOrUpdate(cfg);
	}

	private boolean compareDefinitions(ProcessDefinitionConfig cfg, ProcessDefinitionConfig c) 
	{
		
		/* process name */
		if (!cfg.getBpmDefinitionKey().equals(c.getBpmDefinitionKey())) 
			return false;
		
		/* process description */
		if (!cfg.getDescription().equals(c.getDescription())) 
			return false;
		
		/* process name */
		if (!cfg.getProcessName().equals(c.getProcessName())) 
			return false;
		
		/* process version */
		if (!cfg.getProcessVersion().equals(c.getProcessVersion())) 
			return false;
		
		/* process comment or task item name */
		if (!Lang.equals(cfg.getComment(), c.getComment()) ||
            !Lang.equals(cfg.getTaskItemClass(), c.getTaskItemClass())) 
			return false;
		
		/* states count */
		if (cfg.getStates().size() != c.getStates().size()) 
			return false;
		
		/* process logo */
        if (!Arrays.equals(cfg.getProcessLogo(), c.getProcessLogo())) 
        	return false;

        
		Map<String,ProcessStateConfiguration> oldMap = new HashMap<String,ProcessStateConfiguration>();
		for (ProcessStateConfiguration s : cfg.getStates()) {
			oldMap.put(s.getName(), s);
		}
		Map<String,ProcessStateConfiguration> newMap = new HashMap<String,ProcessStateConfiguration>();
		for (ProcessStateConfiguration s : c.getStates()) {
			if (!oldMap.containsKey(s.getName())) return false;
			newMap.put(s.getName(), s);
		}
		for (Map.Entry<String, ProcessStateConfiguration> entry : oldMap.entrySet()) {
			String name = entry.getKey();
			if (!newMap.containsKey(name)) return false;
			if (!compareStates(entry.getValue(), newMap.get(name))) return false;
		}
        if (!comparePermissions(cfg.getPermissions(), c.getPermissions())) return false;

		return true;

	}

    private boolean isEqual(String s1, String s2) {
        return s1 == null && s2 == null || !(s1 != null && s2 == null) && !(s2 != null && s1 == null) && s1.equals(s2);
    }
    
    private boolean isEqual(Boolean s1, Boolean s2) {
        return s1 == null && s2 == null || !(s1 != null && s2 == null) && !(s2 != null && s1 == null) && s1.equals(s2);
    }
	private boolean compareStates(ProcessStateConfiguration newState, ProcessStateConfiguration oldState) {
		if (newState.getActions().size() != oldState.getActions().size()) return false;
        if (!isEqual(newState.getDescription(),oldState.getDescription())) return false;
		if (!isEqual(newState.getCommentary(),oldState.getCommentary())) return false;
		if (!isEqual(newState.getCommentary(),oldState.getCommentary())) return false;
		if (!isEqual(newState.getEnableExternalAccess(),oldState.getEnableExternalAccess())) return false;
		
		Map<String,ProcessStateAction> newActionMap = new HashMap<String,ProcessStateAction>();
		for (ProcessStateAction a : newState.getActions()) {
			newActionMap.put(a.getBpmName(), a);
		}
		for (ProcessStateAction a : oldState.getActions()) {
			String name = a.getBpmName();
			if (!newActionMap.containsKey(name)) return false;
			if (!compareActions(newActionMap.get(name), a)) return false;

		}

		Set<ProcessStateWidget> newWidgets = newState.getWidgets();
		Set<ProcessStateWidget> oldWidgets = oldState.getWidgets();

        if (!comparePermissions(oldState.getPermissions(), newState.getPermissions())) return false;

		return compareWidgets(newWidgets, oldWidgets);


	}

	private boolean compareWidgets(Set<ProcessStateWidget> newWidgets, Set<ProcessStateWidget> oldWidgets) {
		if (newWidgets.size() != oldWidgets.size()) return false;
		Map<String,ProcessStateWidget> widgetMap = new HashMap<String,ProcessStateWidget>();
		for (ProcessStateWidget w : newWidgets) {
			widgetMap.put(w.getName()+w.getPriority(), w);
		}
		for (ProcessStateWidget w : oldWidgets) {
			if (!widgetMap.containsKey(w.getName()+w.getPriority())) return false;
			if (!compareWidgets(widgetMap.get(w.getName()+w.getPriority()), w)) return false;
		}
		return true;

	}

	private boolean compareWidgets(ProcessStateWidget newWidget, ProcessStateWidget oldWidget) {
		if (newWidget.getAttributes().size() != oldWidget.getAttributes().size()) return false;
		if (newWidget.getChildren().size() != oldWidget.getChildren().size()) return false;

		Map<String,String> attrVals = new HashMap();
		for (ProcessStateWidgetAttribute a : newWidget.getAttributes()) {
			attrVals.put(a.getName(), a.getValue());
		}
		for (ProcessStateWidgetAttribute a : oldWidget.getAttributes()) {
			if (!attrVals.containsKey(a.getName()) || !attrVals.get(a.getName()).equals(a.getValue())) return false;
		}

		return comparePermissions(newWidget.getPermissions(), oldWidget.getPermissions()) &&
			   compareWidgets(newWidget.getChildren(), oldWidget.getChildren());

	}

	private boolean compareActions(ProcessStateAction newAction, ProcessStateAction oldAction) {
		return
                nvl(newAction.getDescription(),"").equals(nvl(oldAction.getDescription(), "")) &&
                nvl(newAction.getButtonName(),"").equals(nvl(oldAction.getButtonName(), "")) &&
                nvl(newAction.getBpmName(),"").equals(nvl(oldAction.getBpmName(), "")) &&
                nvl(newAction.getAutohide(),false).equals(nvl(oldAction.getAutohide(),false)) &&
                nvl(newAction.getSkipSaving(),false).equals(nvl(oldAction.getSkipSaving(),false)) &&
                nvl(newAction.getLabel(),"").equals(nvl(oldAction.getLabel(), "")) &&
                nvl(newAction.getNotification(),"").equals(nvl(oldAction.getNotification(), "")) &&
                Lang.equals(newAction.getMarkProcessImportant(), oldAction.getMarkProcessImportant()) &&
                nvl(newAction.getPriority(),0).equals(nvl(oldAction.getPriority(), 0)) &&
                compareAttributes(newAction.getAttributes(), oldAction.getAttributes()) &&
				comparePermissions(newAction.getPermissions(), oldAction.getPermissions());

	}

    private boolean compareAttributes(Set<ProcessStateActionAttribute> attributes, Set<ProcessStateActionAttribute> attributes1) {
        Map<String,String> attrVals = new HashMap();
        for (ProcessStateActionAttribute a : attributes) {
            attrVals.put(a.getName(), a.getValue());
        }
        for (ProcessStateActionAttribute a : attributes1) {
            if (!attrVals.containsKey(a.getName()) || !attrVals.get(a.getName()).equals(a.getValue())) return false;
        }
        return true;
    }

    private boolean comparePermissions(Set<? extends IPermission> newPermissions, Set<? extends IPermission> oldPermissions) 
    {
		if (newPermissions.size() != oldPermissions.size()) 
			return false;
		Set<String> permissionSet = new HashSet<String>();
		
		for (IPermission p : newPermissions) 
			permissionSet.add(p.getPrivilegeName() + "|||" + p.getRoleName());
		
		for (IPermission p : oldPermissions) 
			if (!permissionSet.contains(p.getPrivilegeName() + "|||" + p.getRoleName())) 
				return false;

		return true;
	}


	private void cleanupWidgetsTree(Set<ProcessStateWidget> widgets, ProcessStateWidget parent,
	                                Set<ProcessStateWidget> processed) {
		if (widgets != null) for (ProcessStateWidget stateWidget : widgets) {
			if (processed.contains(stateWidget)) {
				throw new RuntimeException("Error for config, recursive process state widget tree!");
			}
			if (stateWidget.getPermissions() != null) for (ProcessStateWidgetPermission p : stateWidget.getPermissions()) {
				p.setWidget(stateWidget);
			}
			if (stateWidget.getAttributes() != null) for (ProcessStateWidgetAttribute a : stateWidget.getAttributes()) {
				a.setWidget(stateWidget);
			}
			stateWidget.setParent(parent);
			processed.add(stateWidget);
			cleanupWidgetsTree(stateWidget.getChildren(), stateWidget, processed);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
    public void updateOrCreateQueueConfigs(Collection<ProcessQueueConfig> cfgs) {
        Session session = getSession();
		for (ProcessQueueConfig q : cfgs) {
			List<ProcessQueueConfig> queues = session.createCriteria(ProcessQueueConfig.class)
					.add(Restrictions.eq("name", q.getName())).list();
			

			for (Object o :queues) 
				session.delete(o);
			
			for (ProcessQueueRight r : q.getRights()) 
				r.setQueue(q);
			

			session.save(q);
		}
	}

    @SuppressWarnings("unchecked")
	@Override
   public void removeQueueConfigs(Collection<ProcessQueueConfig> cfgs) {
       Session session = getSession();
       for (ProcessQueueConfig q : cfgs) {
           List<ProcessQueueConfig> queues = session.createCriteria(ProcessQueueConfig.class)
                   .add(Restrictions.eq("name", q.getName())).list();
           for (Object o : queues) {
               session.delete(o);
           }
       }
   }

    @Override
    public Collection<ProcessDefinitionConfig> getConfigurationVersions(ProcessDefinitionConfig cfg) {
        return session.createCriteria(ProcessDefinitionConfig.class)
        						.add(Restrictions.eq("bpmDefinitionKey", cfg.getBpmDefinitionKey()))
                        .list();
    }

    @Override
    public void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled) {

        cfg = (ProcessDefinitionConfig) session.get(ProcessDefinitionConfig.class, cfg.getId());
        cfg.setEnabled(enabled);
        session.save(cfg);
    }
}
