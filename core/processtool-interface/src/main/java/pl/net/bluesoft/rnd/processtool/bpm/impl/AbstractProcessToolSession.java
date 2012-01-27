package pl.net.bluesoft.rnd.processtool.bpm.impl;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.*;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.Mapcar;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public abstract class AbstractProcessToolSession implements ProcessToolBpmSession, Serializable {

    protected Logger log = Logger.getLogger(ProcessToolBpmSession.class.getName());

    /**
     * User and role names are provided externally, e.g. from Liferay.
     * Of course, the implementation can load the roleNames by itself.
     */
    protected UserData user;
    protected Collection<String> roleNames;
    protected UserData substitutingUser;

    protected EventBusManager eventBusManager;

//    protected ProcessDefinitionDAO processDefinitionDAO;
//    protected ProcessInstanceDAO processInstanceDAO;

    /**
     * @param user
     * @param roleNames
     */
    public AbstractProcessToolSession(UserData user, Collection<String> roleNames) {
        this.user = user;
        this.roleNames = new HashSet<String>(roleNames);
        this.eventBusManager = new EventBusManager();
        log.finest("Created session for user: " + user);
    }

    public ProcessInstance createProcessInstance(ProcessDefinitionConfig config, String externalKey, ProcessToolContext ctx,
                                                 String description,
                                                 String keyword,
                                                 String source) {
        if (!config.getEnabled()) {
            throw new IllegalArgumentException("Process definition has been disabled!");
        }
        ProcessInstance pi = new ProcessInstance();
	    pi.setDefinition(config);
	    pi.setCreator(user);
	    pi.setDefinitionName(config.getBpmDefinitionKey());
	    pi.setCreateDate(new Date());
	    pi.setExternalKey(externalKey);
	    pi.setDescription(description);
	    pi.setKeyword(keyword);

        if (user != null) {
            ProcessInstanceSimpleAttribute attr = new ProcessInstanceSimpleAttribute();
            attr.setKey("creator");
            attr.setValue(user.getLogin());
            pi.addAttribute(attr);

            attr = new ProcessInstanceSimpleAttribute();
            attr.setKey("creatorName");
            attr.setValue(user.getRealName());
            pi.addAttribute(attr);
        }
        ProcessInstanceSimpleAttribute attr = new ProcessInstanceSimpleAttribute();
        attr.setKey("source");
        attr.setValue(source);
        pi.addAttribute(attr);

        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
	    pi = startProcessInstance(config, externalKey, ctx, pi);

	    pi.setState(getProcessState(pi, ctx));

	    ProcessInstanceLog log = new ProcessInstanceLog();
	    log.setState(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(pi));
	    log.setEntryDate(Calendar.getInstance());
	    log.setEventI18NKey("process.log.process-started");
	    log.setUser(ctx.getProcessInstanceDAO().findOrCreateUser(user));
	    log.setLogType(ProcessInstanceLog.LOG_TYPE_START_PROCESS);

	    pi.addProcessLog(log);

        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        eventBusManager.publish(new BpmEvent(BpmEvent.Type.NEW_PROCESS, pi, user));
	    ctx.getEventBusManager().publish(new BpmEvent(BpmEvent.Type.NEW_PROCESS, pi, user));

        return pi;
    }

    public ProcessStateConfiguration getProcessStateConfiguration(ProcessInstance pi, ProcessToolContext ctx) {
        ProcessStateConfiguration configuration = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(pi);
        ProcessStateConfiguration res = new ProcessStateConfiguration();
        res.setDescription(configuration.getDescription());
        res.setCommentary(configuration.getCommentary());
        res.setName(configuration.getName());
        Set<ProcessStateWidget> newWidgetList = new HashSet();
        for (ProcessStateWidget widget : configuration.getWidgets()) {
            Set<ProcessStateWidgetPermission> permissions = widget.getPermissions();
            if (permissions.isEmpty()) {
                newWidgetList.add(widget);
            }
            else {
                if (!getPermissionsForWidget(widget, ctx).isEmpty()) {
                    newWidgetList.add(widget);
                }
            }
        }
        res.setWidgets(newWidgetList);

        //actions
        Set<ProcessStateAction> actionList = new HashSet();
        for (ProcessStateAction a : configuration.getActions()) {
            if (a.getPermissions().isEmpty()) {
                actionList.add(a);
            }
            else {
                if (!getPermissionsForAction(a, ctx).isEmpty()) {
                    actionList.add(a);
                }
            }
        }
        res.setActions(actionList);
        return res;
    }

    protected Set<String> getPermissions(Collection<? extends AbstractPermission> col) {
        Set<String> res = new HashSet<String>();
        for (AbstractPermission permission : col) {
            if (hasMatchingRole(permission.getRoleName())) {
                res.add(permission.getPriviledgeName());
            }
        }
        return res;
    }

    public Set<String> getPermissionsForWidget(ProcessStateWidget widget, ProcessToolContext ctx) {
        return getPermissions(widget.getPermissions());
    }

    public Set<String> getPermissionsForAction(ProcessStateAction action, ProcessToolContext ctx) {
        return getPermissions(action.getPermissions());
    }

    public EventBusManager getEventBusManager() {
        return eventBusManager;
    }

	public String getUserLogin() {
		return user.getLogin();
	}
    @Override
    public UserData getUser(ProcessToolContext ctx) {
        return ctx.getUserDataDAO().loadOrCreateUserByLogin(user);
    }

    @Override
    public UserData getSubstitutingUser(ProcessToolContext ctx) {
        return substitutingUser != null ? ctx.getUserDataDAO().loadOrCreateUserByLogin(substitutingUser) : null;
    }

    @Override
    public ProcessInstance getProcessData(String internalId, ProcessToolContext ctx) {
        return ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
    }

    @Override
    public void saveProcessInstance(ProcessInstance processInstance, ProcessToolContext ctx) {
        ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
    }

    protected abstract ProcessInstance startProcessInstance(ProcessDefinitionConfig config, String externalKey,
                                                            ProcessToolContext ctx, ProcessInstance pi);

    public Collection<ProcessDefinitionConfig> getAvailableConfigurations(ProcessToolContext ctx) {
        Collection<ProcessDefinitionConfig> activeConfigurations = ctx.getProcessDefinitionDAO().getActiveConfigurations();
        List<ProcessDefinitionConfig> res = new ArrayList<ProcessDefinitionConfig>();
        for (ProcessDefinitionConfig cfg : activeConfigurations) {
            if (cfg.getPermissions().isEmpty()) {
                res.add(cfg);
            }
            for (ProcessDefinitionPermission permission : cfg.getPermissions()) {
                String roleName = permission.getRoleName();
                if ("RUN".equals(permission.getPriviledgeName()) && roleName != null && hasMatchingRole(roleName)) {
                    res.add(cfg);
                    break;
                }
            }
        }
        return res;
    }

    protected Collection<ProcessQueue> getUserQueuesFromConfig(ProcessToolContext ctx) {

        return new Mapcar<ProcessQueueConfig, ProcessQueue>(ctx.getProcessDefinitionDAO().getQueueConfigs()) {
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

                    if (hasMatchingRole(rn)) {
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
                ProcessQueue pq = new ProcessQueue();
                pq.setBrowsable(browsable);
                pq.setName(x.getName());
	            pq.setDescription(x.getDescription());
                pq.setProcessCount(0);
                return pq;
            }
        }.go();
    }

    private boolean hasMatchingRole(String roleName) {
        for (String role : roleNames) {
            if (role != null && role.matches(roleName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<String> getRoleNames() {
        return roleNames;
    }
}
