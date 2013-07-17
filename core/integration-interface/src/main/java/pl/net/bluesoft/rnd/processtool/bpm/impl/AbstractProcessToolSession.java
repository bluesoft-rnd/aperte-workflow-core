package pl.net.bluesoft.rnd.processtool.bpm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.event.IEvent;
import pl.net.bluesoft.rnd.processtool.event.ProcessToolEventBusManager;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueueBean;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Mapcar;
import pl.net.bluesoft.util.lang.Pair;
import pl.net.bluesoft.util.lang.Predicate;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public abstract class AbstractProcessToolSession implements ProcessToolBpmSession, Serializable {

    protected Logger log = Logger.getLogger(ProcessToolBpmSession.class.getName());

    /**
     * User and role names are provided externally, e.g. from Liferay.
     * Of course, the implementation can load the roleNames by itself.
     */
    protected UserData user;
    protected EventBusManager eventBusManager;
    protected Collection<String> roleNames;

    protected UserData substitutingUser;
    protected EventBusManager substitutingUserEventBusManager;

    @Autowired
    private ProcessToolRegistry processToolRegistry;

    protected AbstractProcessToolSession(UserData user, Collection<String> roleNames) {
    	SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        this.user = user;
        this.roleNames = new HashSet<String>(roleNames);
        this.eventBusManager = new ProcessToolEventBusManager(processToolRegistry, processToolRegistry.getExecutorService());
        log.finest("Created session for user: " + user);
    }

    protected void broadcastEvent(IEvent event) {
        eventBusManager.publish(event);
        if (substitutingUserEventBusManager != null) {
			substitutingUserEventBusManager.publish(event);
		}
    }

	protected UserData findOrCreateUser(UserData user)
	{
		return getContext().getUserDataDAO().loadOrCreateUserByLogin(user);
	}

    protected Set<String> getPermissions(Collection<? extends IPermission> col) {
        Set<String> res = new HashSet<String>();
        for (IPermission permission : col) {
            if (hasMatchingRole(permission.getRoleName())) {
                res.add(permission.getPrivilegeName());
            }
        }
        return res;
    }

    @Override
	public Set<String> getPermissionsForWidget(ProcessStateWidget widget) {
        return getPermissions(widget.getPermissions());
    }

    @Override
	public Set<String> getPermissionsForAction(ProcessStateAction action) {
        return getPermissions(action.getPermissions());
    }

    @Override
	public boolean hasPermissionsForDefinitionConfig(ProcessDefinitionConfig config) {
        if (config.getPermissions() == null || config.getPermissions().isEmpty()) {
            return true;
        }

        Pair<Collection<ProcessDefinitionPermission>, Collection<ProcessDefinitionPermission>> pair =
                Collections.halve(config.getPermissions(), new Predicate<ProcessDefinitionPermission>() {
                    @Override
                    public boolean apply(ProcessDefinitionPermission input) {
                        return PRIVILEGE_EXCLUDE.equalsIgnoreCase(input.getPrivilegeName());
                    }
                });

        Collection<ProcessDefinitionPermission> excludes = pair.getFirst();
        if (!excludes.isEmpty()) {
            ProcessDefinitionPermission permission = Collections.firstMatching(excludes, new Predicate<ProcessDefinitionPermission>() {
                @Override
                public boolean apply(ProcessDefinitionPermission input) {
                    return hasMatchingRole(input.getRoleName());
                }
            });
            if (permission != null) {
                return false;
            }
        }
        Collection<ProcessDefinitionPermission> includes = pair.getSecond();
        ProcessDefinitionPermission permission = Collections.firstMatching(includes, new Predicate<ProcessDefinitionPermission>() {
            @Override
            public boolean apply(ProcessDefinitionPermission input) {
                return hasMatchingRole(input.getRoleName());
            }
        });

        return permission != null || includes.isEmpty();
    }

    @Override
	public EventBusManager getEventBusManager() {
        return eventBusManager;
    }

    @Override
	public String getUserLogin() {
        return user.getLogin();
    }

    @Override
    public UserData getUser() {
        user = loadOrCreateUser(user);
        return user;
    }

    @Override
    public UserData loadOrCreateUser(UserData userData) {
        return findOrCreateUser(userData);
    }

    @Override
    public UserData getSubstitutingUser() {
        return substitutingUser != null ? findOrCreateUser(substitutingUser) : null;
    }

    @Override
    public ProcessInstance getProcessData(String internalId) {
        return getContext().getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
    }

    @Override
	public ProcessInstance refreshProcessData(ProcessInstance pi) {
        return getContext().getProcessInstanceDAO().refreshProcessInstance(pi);
    }

    @Override
    public void saveProcessInstance(ProcessInstance processInstance) {
		getContext().updateContext(processInstance);
		getContext().getProcessInstanceDAO().saveProcessInstance(processInstance);
    }

    @Override
	public Collection<ProcessDefinitionConfig> getAvailableConfigurations() {
        Collection<ProcessDefinitionConfig> activeConfigurations = getContext().getProcessDefinitionDAO().getActiveConfigurations();
        List<ProcessDefinitionConfig> res = new ArrayList<ProcessDefinitionConfig>();
        for (ProcessDefinitionConfig cfg : activeConfigurations) {
            if (cfg.getPermissions().isEmpty()) {
                res.add(cfg);
    		}
            for (ProcessDefinitionPermission permission : cfg.getPermissions()) {
                String roleName = permission.getRoleName();
                if ("RUN".equals(permission.getPrivilegeName()) && roleName != null && hasMatchingRole(roleName)) {
                    res.add(cfg);
                    break;
                }
            }
        }
        java.util.Collections.sort(res, ProcessDefinitionConfig.DEFAULT_COMPARATOR);
        return res;
    }

    protected List<ProcessQueue> getUserQueuesFromConfig() {
		return getQueuesFromConfig(roleNames);
    }

	public static List<ProcessQueue> getQueuesFromConfig(final Collection<String> roleNames) {
		Collection<ProcessQueueConfig> queueConfigs = getContext().getProcessDefinitionDAO().getQueueConfigs();
		return new Mapcar<ProcessQueueConfig, ProcessQueue>(queueConfigs) {
            @Override
            public ProcessQueue lambda(ProcessQueueConfig x) {
                if (x.getRights().isEmpty()) {
                    return transform(x, false);
                }
                boolean found = false;
                boolean browsable = false;
                for (ProcessQueueRight r : x.getRights()) {
                    String rn = r.getRoleName();
                    if (rn == null) {
                        continue;
                    }

                    if (hasMatchingRole(rn, roleNames)) {
                        found = true;
                        browsable = browsable || r.isBrowseAllowed();
                    }
                }
                if (found) {
                    return transform(x, browsable);
                }
                return null;
            }

            ProcessQueue transform(ProcessQueueConfig x, boolean browsable) {
				return createProcessQueue(x, browsable);
            }
        }.go();
	}

	private static ProcessQueue createProcessQueue(ProcessQueueConfig x, boolean browsable) {
		ProcessQueueBean pq = new ProcessQueueBean();
		pq.setBrowsable(browsable);
		pq.setName(x.getName());
		pq.setDescription(x.getDescription());
		pq.setProcessCount(0);
		pq.setUserAdded(nvl(x.getUserAdded(), false));
		return pq;
	}

	private boolean hasMatchingRole(String roleName) {
		return hasMatchingRole(roleName, roleNames);
    }

	private static boolean hasMatchingRole(String roleName, Collection<String> roleNames) {
		for (String role : roleNames) {
            if (role != null && role.matches(roleName)) {
                return true;
            }
        }
		return false;
	}

	@Override
    public Collection<String> getRoleNames() {
        return java.util.Collections.unmodifiableCollection(roleNames);
    }

	protected static ProcessToolContext getContext() {
		return ProcessToolContext.Util.getThreadProcessToolContext();
	}
}
