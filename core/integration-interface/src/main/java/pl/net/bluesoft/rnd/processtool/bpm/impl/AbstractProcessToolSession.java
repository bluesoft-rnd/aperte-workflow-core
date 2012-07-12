package pl.net.bluesoft.rnd.processtool.bpm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.event.ProcessToolEventBusManager;
import pl.net.bluesoft.rnd.processtool.hibernate.TransactionFinishedCallback;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessStatus;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.AbstractPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionPermission;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.util.eventbus.EventBusManager;
import pl.net.bluesoft.util.lang.Collections;
import pl.net.bluesoft.util.lang.Mapcar;
import pl.net.bluesoft.util.lang.Pair;
import pl.net.bluesoft.util.lang.Predicate;

/**
 * @author tlipski@bluesoft.net.pl, mpawlak@bluesoft.net.pl
 */
public abstract class AbstractProcessToolSession
        implements ProcessToolBpmSession, Serializable {

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

    public AbstractProcessToolSession(UserData user, Collection<String> roleNames, ProcessToolRegistry registry) {
        this.user = user;
        this.roleNames = new HashSet<String>(roleNames);
        this.eventBusManager = new ProcessToolEventBusManager(registry, registry.getExecutorService());
        log.finest("Created session for user: " + user);
    }
    
    /** Method creates new subprocess instance of given parent process. The creator of new process
     * is set to the parent process creator. The parent process list is updated with newly created
     * child process instance
     * 
     */
    public ProcessInstance createSubprocessInstance(ProcessDefinitionConfig config, ProcessToolContext ctx,
			ProcessInstance parentProcessInstance, String source, String id) 
    {

    	ProcessInstance newSubprocessInstance = createProcessInstance(config, null, ctx, null, null, source, id, parentProcessInstance.getCreator());
    	
    	/** Corelate parent process with it's new child process */ 
    	newSubprocessInstance.setParent(parentProcessInstance);  	
    	parentProcessInstance.getChildren().add(newSubprocessInstance);
    	
    	return newSubprocessInstance;
	}

	/**
	 * Methods crates a new process instance and sets the creator to current
	 * context user
	 */
    public ProcessInstance createProcessInstance(ProcessDefinitionConfig config,String externalKey, ProcessToolContext ctx,
            String description, String keyword, String source, String internalId)
    {
    	return createProcessInstance(config, externalKey, ctx, description, keyword, source, internalId, user);
    }

    public ProcessInstance createProcessInstance(ProcessDefinitionConfig config,
                                                 String externalKey,
                                                 ProcessToolContext ctx,
                                                 String description, String keyword,
                                                 String source, String internalId, UserData creator) {
        
    	
    	/** If given configuration is disabled, throw exception */
    	if (!config.getEnabled()) 
            throw new IllegalArgumentException("Process definition has been disabled!");
        
        ProcessInstance pi = new ProcessInstance();
        pi.setDefinition(config);
        pi.setCreator(creator);
        pi.setDefinitionName(config.getBpmDefinitionKey());
        pi.setCreateDate(new Date());
        pi.setExternalKey(externalKey);
        pi.setDescription(description);
        pi.setKeyword(keyword);
        pi.setStatus(ProcessStatus.NEW);

        if (creator != null) {
            ProcessInstanceSimpleAttribute attr = new ProcessInstanceSimpleAttribute();
            attr.setKey("creator");
            attr.setValue(creator.getLogin());
            pi.addAttribute(attr);

            attr = new ProcessInstanceSimpleAttribute();
            attr.setKey("creatorName");
            attr.setValue(creator.getRealName());
            pi.addAttribute(attr);
        }
        ProcessInstanceSimpleAttribute attr = new ProcessInstanceSimpleAttribute();
        attr.setKey("source");
        attr.setValue(source);
        pi.addAttribute(attr);

        ctx.getProcessInstanceDAO().saveProcessInstance(pi);

        if(internalId == null)
        	pi = startProcessInstance(config, externalKey, ctx, pi);
        else
        	pi.setInternalId(internalId);

        creator = findOrCreateUser(creator, ctx);

        ProcessInstanceLog log = new ProcessInstanceLog();
        log.setState(null);
        log.setEntryDate(Calendar.getInstance());
        log.setEventI18NKey("process.log.process-started");
        log.setUser(creator);
        log.setLogType(ProcessInstanceLog.LOG_TYPE_START_PROCESS);
        //log.setLogType(LogType.START);
        pi.getRootProcessInstance().addProcessLog(log);

        ctx.getProcessInstanceDAO().saveProcessInstance(pi);

        List<BpmEvent> events = new ArrayList<BpmEvent>();
        events.add(new BpmEvent(Type.NEW_PROCESS, pi, creator));

        for (BpmTask task : findProcessTasks(pi, ctx)) {
            events.add(new BpmEvent(Type.ASSIGN_TASK, task, creator));
        }

        for (BpmEvent event : events) {
            broadcastEvent(ctx, event);
        }

        return pi;
    }

    protected void broadcastEvent(final ProcessToolContext ctx, final BpmEvent event) {
        eventBusManager.publish(event);
        if (substitutingUserEventBusManager != null)
            substitutingUserEventBusManager.publish(event);
        ctx.addTransactionCallback(new TransactionFinishedCallback() {
            @Override
            public void onFinished() {
                ctx.getEventBusManager().post(event);
            }
        });
    }

    protected UserData findOrCreateUser(UserData user, ProcessToolContext ctx) {
        return ctx.getProcessInstanceDAO().findOrCreateUser(user);
    }

//    public ProcessStateConfiguration getProcessStateConfiguration(ProcessInstance pi, ProcessToolContext ctx) {
//        ProcessStateConfiguration configuration = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(pi);
//        if (configuration == null) return null;
//        ProcessStateConfiguration res = new ProcessStateConfiguration();
//        res.setDescription(configuration.getDescription());
//        res.setCommentary(configuration.getCommentary());
//        res.setName(configuration.getName());
//        Set<ProcessStateWidget> newWidgetList = new HashSet<ProcessStateWidget>();
//        for (ProcessStateWidget widget : configuration.getWidgets()) {
//            Set<ProcessStateWidgetPermission> permissions = widget.getPermissions();
//            if (permissions.isEmpty()) {
//                newWidgetList.add(widget);
//            }
//            else {
//                if (!getPermissionsForWidget(widget, ctx).isEmpty()) {
//                    newWidgetList.add(widget);
//                }
//            }
//        }
//        res.setWidgets(newWidgetList);
//
//        //actions
//        Set<ProcessStateAction> actionList = new HashSet<ProcessStateAction>();
//        for (ProcessStateAction a : configuration.getActions()) {
//            if (a.getPermissions().isEmpty()) {
//                actionList.add(a);
//            }
//            else {
//                if (!getPermissionsForAction(a, ctx).isEmpty()) {
//                    actionList.add(a);
//                }
//            }
//        }
//        res.setActions(actionList);
//        return res;
//    }

    protected Set<String> getPermissions(Collection<? extends AbstractPermission> col) {
        Set<String> res = new HashSet<String>();
        for (AbstractPermission permission : col) {
            if (hasMatchingRole(permission.getRoleName())) {
                res.add(permission.getPrivilegeName());
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

    public EventBusManager getEventBusManager() {
        return eventBusManager;
    }

    public String getUserLogin() {
        return user.getLogin();
    }

    @Override
    public UserData getUser(ProcessToolContext ctx) {
        user = loadOrCreateUser(ctx, user);
        return user;
    }

    @Override
    public UserData loadOrCreateUser(ProcessToolContext ctx, UserData userData) {
        return findOrCreateUser(userData, ctx);
    }

    @Override
    public UserData getSubstitutingUser(ProcessToolContext ctx) {
        return substitutingUser != null ? findOrCreateUser(substitutingUser, ctx) : null;
    }

    @Override
    public ProcessInstance getProcessData(String internalId, ProcessToolContext ctx) {
        return ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(internalId);
    }

    public ProcessInstance refreshProcessData(ProcessInstance pi, ProcessToolContext ctx) {
        return ctx.getProcessInstanceDAO().refreshProcessInstance(pi);
    }

    @Override
    public void saveProcessInstance(pl.net.bluesoft.rnd.processtool.model.ProcessInstance
                                                processInstance, ProcessToolContext ctx) {
        ctx.updateContext(processInstance);
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
                if ("RUN".equals(permission.getPrivilegeName()) && roleName != null && hasMatchingRole(roleName)) {
                    res.add(cfg);
                    break;
                }
            }
        }
        java.util.Collections.sort(res);
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
                pq.setUserAdded(x.getUserAdded());
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

    protected ProcessToolContext getCurrentContext() {
        return ProcessToolContext.Util.getThreadProcessToolContext();
    }


    @Override
    public Collection<String> getRoleNames() {
        return roleNames;
    }

}
