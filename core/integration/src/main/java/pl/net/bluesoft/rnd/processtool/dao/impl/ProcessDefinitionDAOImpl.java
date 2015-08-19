package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.util.CollectionComparer;
import pl.net.bluesoft.util.lang.ExpiringCache;

import java.util.*;

import static pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig.*;
import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessDefinitionDAOImpl extends SimpleHibernateBean<ProcessDefinitionConfig>
        implements ProcessDefinitionDAO {

	private static final ExpiringCache<Long, ProcessDefinitionConfig> DEFINITION_BY_ID =
			new ExpiringCache<Long, ProcessDefinitionConfig>(Long.MAX_VALUE);

	private static final ExpiringCache<String, ProcessDefinitionConfig> DEFINITION_BY_BPMKEY =
			new ExpiringCache<String, ProcessDefinitionConfig>(Long.MAX_VALUE);

	private static final ExpiringCache<Long, ProcessStateConfiguration> STATE_BY_ID =
			new ExpiringCache<Long, ProcessStateConfiguration>(Long.MAX_VALUE);

	private static final ExpiringCache<Long, ProcessStateWidget> WIDGET_BY_ID =
			new ExpiringCache<Long, ProcessStateWidget>(Long.MAX_VALUE);

	private static final ExpiringCache<Object, List<ProcessQueueConfig>> QUEUE_CONFIGS =
			new ExpiringCache<Object, List<ProcessQueueConfig>>(Long.MAX_VALUE);

	private static final ExpiringCache<Object, Map<Long, String>> ALL_DEFINITION_DESCRIPTIONS =
			new ExpiringCache<Object, Map<Long, String> >(Long.MAX_VALUE);

	private static final ExpiringCache<Object, Map<Long, String> > ALL_STATES_DESCRIPTIONS =
			new ExpiringCache<Object, Map<Long, String> >(Long.MAX_VALUE);

	// null -> priviledge -> bpmDefinitionId -> roleName
	private static final ExpiringCache<Object, Map<String, Map<String, String>>> DEFINITIONS_WITH_PRIVILEDGES =
			new ExpiringCache<Object, Map<String, Map<String, String>>>(Long.MAX_VALUE);

	public ProcessDefinitionDAOImpl(Session session) {
		super(session);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ProcessDefinitionConfig> getAllConfigurations() {
		return getSession().createCriteria(ProcessDefinitionConfig.class).addOrder(Order.desc(_DESCRIPTION)).list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Collection<ProcessDefinitionConfig> getActiveConfigurations() {
		return getSession().createCriteria(ProcessDefinitionConfig.class)
				.addOrder(Order.desc(_DESCRIPTION))
				.add(Restrictions.eq(_LATEST, Boolean.TRUE))
				.add(Restrictions.eq(_ENABLED, Boolean.TRUE))
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .list();
	}

	@Override
	public ProcessDefinitionConfig getActiveConfigurationByKey(String key) {
		return (ProcessDefinitionConfig) getSession().createCriteria(ProcessDefinitionConfig.class)
				.add(Restrictions.eq(_LATEST, Boolean.TRUE))
				.add(Restrictions.eq(_BPM_DEFINITION_KEY, key)).uniqueResult();
	}

	@Override
	public ProcessDefinitionConfig getConfigurationByProcessId(String processId) {
		return (ProcessDefinitionConfig)getSession().createCriteria(ProcessDefinitionConfig.class)
				.add(Restrictions.eq(_BPM_DEFINITION_KEY, extractBpmDefinitionKey(processId)))
				.add(Restrictions.eq(_BPM_DEFINITION_VERSION, extractBpmDefinitionVersion(processId)))
				.uniqueResult();
	}

	@Override
	public ProcessDefinitionConfig getCachedDefinitionById(Long id) {
		return DEFINITION_BY_ID.get(id, new ExpiringCache.NewValueCallback<Long, ProcessDefinitionConfig>() {
			@Override
			public ProcessDefinitionConfig getNewValue(Long id) {
				ProcessDefinitionConfig config = (ProcessDefinitionConfig)getSession().createCriteria(ProcessDefinitionConfig.class)
						.add(Restrictions.eq(_ID, id))
						.setFetchMode(_STATES, FetchMode.EAGER)
						.setFetchMode(_PERMISSIONS, FetchMode.EAGER)
						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
						.uniqueResult();

				if (config != null) {
					fillCaches(config);
				}

				return config;
			}
		});
	}

	@Override
	public ProcessDefinitionConfig getCachedDefinitionByBpmKey(String key) {
		return DEFINITION_BY_BPMKEY.get(key, new ExpiringCache.NewValueCallback<String, ProcessDefinitionConfig>() {
			@Override
			public ProcessDefinitionConfig getNewValue(String key) {
				ProcessDefinitionConfig config = (ProcessDefinitionConfig)getSession().createCriteria(ProcessDefinitionConfig.class)
						.add(Restrictions.eq(_BPM_DEFINITION_KEY, key))
						.add(Restrictions.eq(_LATEST, Boolean.TRUE))
						.add(Restrictions.eq(_ENABLED, Boolean.TRUE))
						.setFetchMode(_STATES, FetchMode.EAGER)
						.setFetchMode(_PERMISSIONS, FetchMode.EAGER)
						.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
						.uniqueResult();

				if (config != null) {
					fillCaches(config);
				}

				return config;
			}
		});
	}

	private void fillCaches(ProcessDefinitionConfig config) {
		for (ProcessStateConfiguration state : config.getStates()) {
			STATE_BY_ID.put(state.getId(), state);

			for (ProcessStateWidget widget : state.getWidgets()) {
				fillWidgetCache(widget);
			}
		}
	}

	private void fillWidgetCache(ProcessStateWidget widget) {
		WIDGET_BY_ID.put(widget.getId(), widget);
		for (ProcessStateWidget childWidget : widget.getChildren()) {
			fillWidgetCache(childWidget);
		}
	}

	@Override
	public ProcessDefinitionConfig getCachedDefinitionById(ProcessInstance processInstance) {
		return getCachedDefinitionById(processInstance.getDefinition().getId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<ProcessQueueConfig> getQueueConfigs() {
		return QUEUE_CONFIGS.get(null, new ExpiringCache.NewValueCallback<Object, List<ProcessQueueConfig>>() {
			@Override
			public List<ProcessQueueConfig> getNewValue(Object key) {
				return getSession().createCriteria(ProcessQueueConfig.class).list();
			}
		});
	}

	@Override
	public void updateOrCreateProcessDefinitionConfig(ProcessDefinitionConfig cfg) {
		cfg.setCreateDate(new Date());
		cfg.setLatest(true);
		cfg.setEnabled(true);

		adjustStatesAndPermissions(cfg);

		ProcessDefinitionConfig latestDefinition = getLatestDefinition(cfg);

		if (latestDefinition != null) {
			latestDefinition.setLatest(false);
			getSession().saveOrUpdate(latestDefinition);
		}

		cfg.setBpmDefinitionVersion(getNextProcessVersion(cfg.getBpmDefinitionKey()));
		getSession().saveOrUpdate(cfg);

		ALL_DEFINITION_DESCRIPTIONS.clear();
		ALL_STATES_DESCRIPTIONS.clear();
		DEFINITION_BY_BPMKEY.clear();
		DEFINITION_BY_ID.clear();
	}


	@Override
	public boolean differsFromTheLatest(ProcessDefinitionConfig cfg) {
		ProcessDefinitionConfig latestDefinition = getLatestDefinition(cfg);
		return latestDefinition == null || !compareDefinitions(cfg, latestDefinition);
	}

	private ProcessDefinitionConfig getLatestDefinition(ProcessDefinitionConfig cfg) {
		return (ProcessDefinitionConfig)getSession().createCriteria(ProcessDefinitionConfig.class)
				.add(Restrictions.eq(_LATEST, true))
				.add(Restrictions.eq(_BPM_DEFINITION_KEY, cfg.getBpmDefinitionKey()))
				.uniqueResult();
	}

	private void adjustStatesAndPermissions(ProcessDefinitionConfig cfg) {
		for (ProcessStateConfiguration state : cfg.getStates()) {
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
	}

	private boolean compareDefinitions(ProcessDefinitionConfig config1, ProcessDefinitionConfig config2) {
		return isEqual(config1.getBpmDefinitionKey(), config2.getBpmDefinitionKey()) &&
				isEqual(config1.getDescription(), config2.getDescription()) &&
				isEqual(config1.getComment(), config2.getComment()) &&
				isEqual(config1.getDefaultStepInfoPattern(), config2.getDefaultStepInfoPattern()) &&
				isEqual(config1.getSupportedLocales(), config2.getSupportedLocales()) &&
				isEqual(config1.getExternalKeyPattern(), config2.getExternalKeyPattern()) &&
				isEqual(config1.getProcessGroup(), config2.getProcessGroup()) &&
				Arrays.equals(config1.getProcessLogo(), config2.getProcessLogo()) &&
				STATE_COMPARER.compare(config1.getStates(), config2.getStates()) &&
				PERMISSION_COMPARER.compare(config1.getPermissions(), config2.getPermissions());
	}

	private CollectionComparer<ProcessStateConfiguration> STATE_COMPARER = new CollectionComparer<ProcessStateConfiguration>() {
		@Override
		protected String getKey(ProcessStateConfiguration state) {
			return state.getName();
		}

		@Override
		protected boolean compareItems(ProcessStateConfiguration state1, ProcessStateConfiguration state2) {
			return isEqual(state1.getDescription(), state2.getDescription()) &&
					isEqual(state1.getCommentary(), state2.getCommentary()) &&
					isEqual(state1.getCommentary(), state2.getCommentary()) &&
					isEqual(state1.getEnableExternalAccess(), state2.getEnableExternalAccess()) &&
					isEqual(state1.getStepInfoPattern(), state2.getStepInfoPattern()) &&
					WIDGET_COMPARER.compare(state1.getWidgets(), state2.getWidgets()) &&
					ACTION_COMPARER.compare(state1.getActions(), state2.getActions()) &&
					PERMISSION_COMPARER.compare(state2.getPermissions(), state1.getPermissions());
		}
	};

	private CollectionComparer<ProcessStateWidget> WIDGET_COMPARER = new CollectionComparer<ProcessStateWidget>() {
		@Override
		protected String getKey(ProcessStateWidget widget) {
			return widget.getName()+widget.getPriority();
		}

		@Override
		protected boolean compareItems(ProcessStateWidget widget1, ProcessStateWidget widget2) {
			return isEqual(widget1.getName(), widget2.getName()) &&
					isEqual(widget1.getClassName(), widget2.getClassName()) &&
					isEqual(widget1.getOptional(), widget2.getOptional()) &&
					isEqual(widget1.getPriority(), widget2.getPriority()) &&
					isEqual(widget1.getGenerateFromCollection(), widget2.getGenerateFromCollection()) &&
					WIDGET_ATTRIBUTE_COMPARER.compare(widget1.getAttributes(), widget2.getAttributes()) &&
					WIDGET_COMPARER.compare(widget1.getChildren(), widget2.getChildren()) &&
					PERMISSION_COMPARER.compare(widget1.getPermissions(), widget2.getPermissions());

		}
	};

	private CollectionComparer<ProcessStateAction> ACTION_COMPARER = new CollectionComparer<ProcessStateAction>() {
		@Override
		protected String getKey(ProcessStateAction action) {
			return action.getBpmName();
		}

		@Override
		protected boolean compareItems(ProcessStateAction action1, ProcessStateAction action2) {
			return isEqual(action1.getDescription(), action2.getDescription()) &&
					isEqual(action1.getButtonName(), action2.getButtonName()) &&
					isEqual(action1.getBpmName(), action2.getBpmName()) &&
					isEqual(action1.getAutohide(), action2.getAutohide()) &&
					isEqual(action1.getSkipSaving(), action2.getSkipSaving()) &&
                    isEqual(action1.getCommentNeeded(), action2.getCommentNeeded()) &&
					isEqual(action1.getLabel(), action2.getLabel()) &&
					isEqual(action1.getNotification(), action2.getNotification()) &&
					isEqual(action1.getMarkProcessImportant(), action2.getMarkProcessImportant()) &&
					isEqual(action1.getPriority(), action2.getPriority()) &&
					ACTION_ATTRIBUTE_COMPARER.compare(action1.getAttributes(), action2.getAttributes()) &&
					PERMISSION_COMPARER.compare(action1.getPermissions(), action2.getPermissions());

		}
	};

	private CollectionComparer<ProcessStateWidgetAttribute> WIDGET_ATTRIBUTE_COMPARER = new CollectionComparer<ProcessStateWidgetAttribute>() {
		@Override
		protected String getKey(ProcessStateWidgetAttribute attribute) {
			return attribute.getName();
		}

		@Override
		protected boolean compareItems(ProcessStateWidgetAttribute attribute1, ProcessStateWidgetAttribute attribute2) {
			return isEqual(attribute1.getValue(), attribute2.getValue());
		}
	};

	private CollectionComparer<ProcessStateActionAttribute> ACTION_ATTRIBUTE_COMPARER = new CollectionComparer<ProcessStateActionAttribute>() {
		@Override
		protected String getKey(ProcessStateActionAttribute attribute) {
			return attribute.getName();
		}

		@Override
		protected boolean compareItems(ProcessStateActionAttribute attribute1, ProcessStateActionAttribute attribute2) {
			return isEqual(attribute1.getValue(), attribute2.getValue());
		}
	};

	private CollectionComparer<IPermission> PERMISSION_COMPARER = new CollectionComparer<IPermission>() {
		@Override
		protected String getKey(IPermission permission) {
			return permission.getPrivilegeName() + "|||" + permission.getRoleName();
		}

		@Override
		protected boolean compareItems(IPermission permission1, IPermission permission2) {
			return true;
		}
	};

	private boolean isEqual(String s1, String s2) {
		return nvl(s1).equals(nvl(s2));
	}

	private boolean isEqual(Boolean s1, Boolean s2) {
		return nvl(s1, false).equals(nvl(s2, false));
	}

	private boolean isEqual(Integer s1, Integer s2) {
		return nvl(s1, 0).equals(nvl(s2, 0));
	}

	private void cleanupWidgetsTree(Set<ProcessStateWidget> widgets, ProcessStateWidget parent,
	                                Set<ProcessStateWidget> processed) {
		if (widgets != null) for (ProcessStateWidget stateWidget : widgets) {
			if (processed.contains(stateWidget)) {
				throw new RuntimeException("Error for config, recursive process state widget tree!");
			}
			if (stateWidget.getPermissions() != null) {
				for (ProcessStateWidgetPermission p : stateWidget.getPermissions()) {
					p.setWidget(stateWidget);
				}
			}
			if (stateWidget.getAttributes() != null) {
				for (ProcessStateWidgetAttribute a : stateWidget.getAttributes()) {
					a.setWidget(stateWidget);
				}
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

			for (ProcessQueueConfig queue : queues) {
				session.delete(queue);
			}
			
			for (ProcessQueueRight r : q.getRights()) {
				r.setQueue(q);
			}

			session.save(q);
		}
		QUEUE_CONFIGS.clear();
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
		QUEUE_CONFIGS.clear();
	}

	@Override
	public int getNextProcessVersion(String bpmDefinitionKey) {
		Object version = session.createCriteria(ProcessDefinitionConfig.class)
				.setProjection(Projections.max(_BPM_DEFINITION_VERSION))
				.add(Restrictions.eq(_BPM_DEFINITION_KEY, bpmDefinitionKey))
				.uniqueResult();
		return version != null ? ((Number)version).intValue() + 1 : 1;
	}

	@Override
    public Collection<ProcessDefinitionConfig> getConfigurationVersions(ProcessDefinitionConfig cfg) {
        return session.createCriteria(ProcessDefinitionConfig.class)
				.add(Restrictions.eq(_BPM_DEFINITION_KEY, cfg.getBpmDefinitionKey()))
                .list();
    }

    @Override
    public void setConfigurationEnabled(ProcessDefinitionConfig cfg, boolean enabled) {
        cfg = (ProcessDefinitionConfig) session.get(ProcessDefinitionConfig.class, cfg.getId());
        cfg.setEnabled(enabled);
        session.save(cfg);
    }

	@Override
	public ProcessStateWidget getCachedProcessStateWidget(final Long widgetStateId) {
		return WIDGET_BY_ID.get(widgetStateId, new ExpiringCache.NewValueCallback<Long, ProcessStateWidget>() {
			@Override
			public ProcessStateWidget getNewValue(Long key) {
				return (ProcessStateWidget) getSession().createCriteria(ProcessStateWidget.class)
						.add(Restrictions.eq("id", widgetStateId))
						.uniqueResult();
			}
		});
	}

	@Override
	public ProcessStateConfiguration getCachedProcessStateConfiguration(final Long processStateConfigurationId) {
		return STATE_BY_ID.get(processStateConfigurationId, new ExpiringCache.NewValueCallback<Long, ProcessStateConfiguration>() {
			@Override
			public ProcessStateConfiguration getNewValue(Long key) {
				return (ProcessStateConfiguration) getSession().createCriteria(ProcessStateConfiguration.class)
						.add(Restrictions.eq("id", processStateConfigurationId))
						.uniqueResult();
			}
		});
	}

	@Override
	public Map<Long, String> getProcessDefinitionDescriptions() {
		return ALL_DEFINITION_DESCRIPTIONS.get(null, new ExpiringCache.NewValueCallback<Object, Map<Long, String>>() {
			@Override
			public Map<Long, String> getNewValue(Object key) {
				List<Object[]> list = session.createCriteria(ProcessDefinitionConfig.class)
						.setProjection(Projections.distinct(Projections.projectionList()
								.add(Projections.property(_ID))
								.add(Projections.property(_DESCRIPTION))
						))
						.list();
				return toMap(list);
			}
		});
	}

	@Override
	public Map<Long, String> getProcessStateDescriptions() {
		return ALL_STATES_DESCRIPTIONS.get(null, new ExpiringCache.NewValueCallback<Object, Map<Long, String>>() {
			@Override
			public Map<Long, String> getNewValue(Object key) {
				List<Object[]> list = session.createCriteria(ProcessStateConfiguration.class)
						.setProjection(Projections.distinct(Projections.projectionList()
								.add(Projections.property(ProcessStateConfiguration._ID))
								.add(Projections.property(ProcessStateConfiguration._DESCRIPTION))
						))
						.list();
				return toMap(list);
			}
		});
	}

	@Override
	public List<String> getNotPermittedDefinitionIds(String priviledgeName, Collection<String> roleNames) {
		Map<String, String> rolesByDefinitionId = DEFINITIONS_WITH_PRIVILEDGES.get(null, new ExpiringCache.NewValueCallback<Object, Map<String, Map<String, String>>>() {
			@Override
			public Map<String, Map<String, String>> getNewValue(Object key) {
				List<Object[]> list = session.createSQLQuery(
						"SELECT p.privilegeName, d.bpmDefinitionKey, d.bpmDefinitionVersion, p.roleName " +
						"FROM pt_process_definition_config d JOIN pt_process_def_prms p ON p.definition_id = d.id")
						.list();

				return groupByPriviledgeAndDefinitionId(list);
			}
		}).get(priviledgeName);

		List<String> result = new ArrayList<String>();

        if(rolesByDefinitionId == null)
            return result;

        for (Map.Entry<String, String> entry : rolesByDefinitionId.entrySet()) {
			if (!hasMatchingRole(entry.getValue(), roleNames)) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	private Map<String, Map<String, String>> groupByPriviledgeAndDefinitionId(List<Object[]> list) {
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

		for (Object[] row : list) {
			String priviledgeName = (String)row[0];
			String bpmDefinitionId = row[1] + VERSION_SEPARATOR + row[2];
			String roleName = (String)row[3];

			if (!result.containsKey(priviledgeName)) {
				result.put(priviledgeName, new HashMap<String, String>());
			}
			result.get(priviledgeName).put(bpmDefinitionId, roleName);
		}
		return result;
	}
}
