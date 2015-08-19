package pl.net.bluesoft.rnd.processtool.bpm.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.event.IEvent;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueueBean;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;
import pl.net.bluesoft.util.lang.Mapcar;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.PRIVILEGE_RUN;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public abstract class AbstractProcessToolSession implements ProcessToolBpmSession, Serializable {
    protected static Logger log = Logger.getLogger(ProcessToolBpmSession.class.getName());

    /**
     * User and role names are provided externally, e.g. from Liferay.
     * Of course, the implementation can load the roleNames by itself.
     */
    protected String userLogin;
    protected Collection<String> roleNames;

    protected String substitutingUserLogin;

    @Autowired
    protected ProcessToolRegistry registry;

    @Autowired
    protected IUserSource userSource;

    protected AbstractProcessToolSession(String userLogin, Collection<String> roleNames, String substitutingUserLogin) {
    	SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        this.userLogin = userLogin;
        this.roleNames = roleNames;
		this.substitutingUserLogin = substitutingUserLogin;
    }

    protected void broadcastEvent(IEvent event) {
        registry.getEventBusManager().publish(event);
    }

    protected Set<String> getPermissions(Collection<? extends IPermission> col) {
        Set<String> res = new HashSet<String>();
        for (IPermission permission : col) {
            if (hasMatchingRole(permission.getRoleName(), getRoleNames())) {
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
		return config.hasPriviledge(PRIVILEGE_RUN, getRoleNames());
	}

    @Override
	public String getUserLogin() {
        return userLogin;
    }

    @Override
    public String getSubstitutingUserLogin() {
        return substitutingUserLogin;
    }

    @Override
	public List<ProcessDefinitionConfig> getAvailableConfigurations() {
        Collection<ProcessDefinitionConfig> activeConfigurations = getContext().getProcessDefinitionDAO().getActiveConfigurations();
        List<ProcessDefinitionConfig> result = new ArrayList<ProcessDefinitionConfig>();
        for (ProcessDefinitionConfig cfg : activeConfigurations) {
			if (cfg.hasPriviledge(PRIVILEGE_RUN, getRoleNames())) {
				result.add(cfg);
			}
        }
        return result;
    }

	public List<ProcessQueue> getUserQueuesFromConfig() {
		return getQueuesFromConfig(getRoleNames(), "admin".equals(userLogin));
    }

	public static List<ProcessQueue> getQueuesFromConfig(final Collection<String> roleNames, final boolean adminUser) {
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

                    if (hasMatchingRole(rn, roleNames) || adminUser) {
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
		if (roleNames == null) {
			UserData user = userSource.getUserByLogin(userLogin);
			roleNames = user != null ? user.getRoles() : Collections.<String>emptySet();
		}
        return Collections.unmodifiableCollection(roleNames);
    }

	protected static ProcessToolContext getContext() {
		return ProcessToolContext.Util.getThreadProcessToolContext();
	}
}
