package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDefinitionDAOImpl extends SimpleHibernateBean<ProcessDefinitionConfig> implements ProcessDefinitionDAO {

	private Logger logger = Logger.getLogger(ProcessDefinitionDAOImpl.class.getName());

	public ProcessDefinitionDAOImpl(Session session) {
		super(session);
	}

	public Collection<ProcessDefinitionConfig> getAllConfigurations() {
		return session.createCriteria(ProcessDefinitionConfig.class).list();
	}

	public Collection<ProcessDefinitionConfig> getActiveConfigurations() {		
		return session.createCriteria(ProcessDefinitionConfig.class)
						.add(Restrictions.eq("latest", Boolean.TRUE)).list();
	}

	@Override
	public ProcessDefinitionConfig getActiveConfigurationByKey(String key) {

		return (ProcessDefinitionConfig) session.createCriteria(ProcessDefinitionConfig.class)
				.add(Restrictions.eq("latest", Boolean.TRUE))
				.add(Restrictions.eq("bpmDefinitionKey", key)).uniqueResult();
	}

	@Override
	public Collection<ProcessQueueConfig> getQueueConfigs() {
		return session.createCriteria(ProcessQueueConfig.class).list();

	}

	public ProcessStateConfiguration getProcessStateConfiguration(ProcessInstance pi) {
//		HibernateTemplate ht = getHibernateTemplate();
		List res = session.createCriteria(ProcessStateConfiguration.class)
				.add(Restrictions.eq("definition", pi.getDefinition()))
				.add(Restrictions.eq("name", pi.getState())).list();
		if (res.isEmpty())
			return null;
		return (ProcessStateConfiguration) res.get(0);

	}

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

	private boolean compareDefinitions(ProcessDefinitionConfig cfg, ProcessDefinitionConfig c) {

		if (!cfg.getBpmDefinitionKey().equals(c.getBpmDefinitionKey())) return false;
		if (!cfg.getDescription().equals(c.getDescription())) return false;
		if (!cfg.getProcessName().equals(c.getProcessName())) return false;
		if (cfg.getStates().size() != c.getStates().size()) return false;

		Map<String,ProcessStateConfiguration> oldMap = new HashMap();
		for (ProcessStateConfiguration s : cfg.getStates()) {
			oldMap.put(s.getName(), s);
		}
		Map<String,ProcessStateConfiguration> newMap = new HashMap();
		for (ProcessStateConfiguration s : c.getStates()) {
			if (!oldMap.containsKey(s.getName())) return false;
			newMap.put(s.getName(), s);
		}
		for (String name : oldMap.keySet()) {
			if (!newMap.containsKey(name)) return false;
			if (!compareStates(oldMap.get(name), newMap.get(name))) return false;
		}

		return true;

	}

    private boolean stringEq(String s1, String s2) {
        return s1 == null && s2 == null || !(s1 != null && s2 == null) && !(s2 != null && s1 == null) && s1.equals(s2);
    }
	private boolean compareStates(ProcessStateConfiguration newState, ProcessStateConfiguration oldState) {
		if (newState.getActions().size() != oldState.getActions().size()) return false;
        if (!stringEq(newState.getDescription(),oldState.getDescription())) return false;
		if (!stringEq(newState.getCommentary(),oldState.getCommentary())) return false;
		if (!stringEq(newState.getCommentary(),oldState.getCommentary())) return false;

		Map<String,ProcessStateAction> newActionMap = new HashMap();
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
		return compareWidgets(newWidgets, oldWidgets);


	}

	private boolean compareWidgets(Set<ProcessStateWidget> newWidgets, Set<ProcessStateWidget> oldWidgets) {
		if (newWidgets.size() != oldWidgets.size()) return false;
		Map<String,ProcessStateWidget> widgetMap = new HashMap();
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
		return newAction.getDescription().equals(oldAction.getDescription()) &&
				comparePermissions(newAction.getPermissions(), oldAction.getPermissions());

	}

	private boolean comparePermissions(Set<? extends AbstractPermission> newPermissions, Set<? extends AbstractPermission> oldPermissions) {
		if (newPermissions.size() != oldPermissions.size()) return false;
		Set<String> permissionSet = new HashSet();
		for (AbstractPermission p : newPermissions) {
			permissionSet.add(p.getPriviledgeName() + "|||" + p.getRoleName());
		}
		for (AbstractPermission p : oldPermissions) {
			if (!permissionSet.contains(p.getPriviledgeName() + "|||" + p.getRoleName())) return false;
		}
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

	@Override
	public void updateOrCreateQueueConfigs(ProcessQueueConfig[] cfgs) {
		for (ProcessQueueConfig q : cfgs) {
			List queues = session.createCriteria(ProcessQueueConfig.class)
					.add(Restrictions.eq("name", q.getName())).list();
			for (Object o :queues) {
				session.delete(o);
			}
			for (ProcessQueueRight r : q.getRights()) {
				r.setQueue(q);
			}
			session.save(q);
		}
	}
}
