package org.aperteworkflow.ext.activiti;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.bpm.graph.StateNode;
import org.aperteworkflow.bpm.graph.TransitionArc;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTask;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.exception.ProcessToolSecurityException;
import pl.net.bluesoft.rnd.processtool.bpm.impl.AbstractProcessToolSession;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.lang.Mapcar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.Lang.keyFilter;
import static pl.net.bluesoft.util.lang.StringUtil.hasText;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivitiBpmSession extends AbstractProcessToolSession {

    private static final Logger LOGGER = Logger.getLogger(ActivitiBpmSession.class.getName());

    /**
     * @param user
     * @param roleNames
     */
    public ActivitiBpmSession(UserData user, Collection<String> roleNames) {
        super(user, roleNames);
        IdentityService is = getProcessEngine().getIdentityService();
        User bpmUser = is.createUserQuery().userId(user.getLogin()).singleResult();
        if (bpmUser == null) {
            bpmUser = is.newUser(user.getLogin());
            bpmUser.setEmail(user.getEmail());
            bpmUser.setFirstName(user.getRealName());
            is.saveUser(bpmUser);
        }
    }

    public String getProcessState(ProcessInstance pi, ProcessToolContext ctx) {
        Task newTask = findProcessTask(pi, ctx);
        return newTask != null ? newTask.getName() : null;
    }

    @Override
    public void saveProcessInstance(ProcessInstance processInstance, ProcessToolContext ctx) {
        RuntimeService es = getProcessEngine().getRuntimeService();
        for (ProcessInstanceAttribute pia : processInstance.getProcessAttributes()) {
            if (pia instanceof BpmVariable) {
                BpmVariable bpmVar = (BpmVariable) pia;
                if (hasText(bpmVar.getBpmVariableName())) {
                    es.setVariable(processInstance.getInternalId(),
                            bpmVar.getBpmVariableName(),
                            bpmVar.getBpmVariableValue());
                }
            }
        }

        super.saveProcessInstance(processInstance, ctx);
    }

    @Override
    public Collection<ProcessInstance> getQueueContents(final ProcessQueue pq, final int offset, final int limit, ProcessToolContext ctx) {
        Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
        final List<String> names = keyFilter("name", configs);
        if (!names.contains(pq.getName())) throw new ProcessToolSecurityException("queue.no.rights", pq.getName());

        TaskService taskService = getProcessEngine().getTaskService();
        List<Task> taskList = taskService.createTaskQuery()
                .taskCandidateGroup(pq.getName())
                .taskUnnassigned()
                .orderByTaskId()
                .desc()
                .listPage(offset, limit);
        List<String> ids = keyFilter("processInstanceId", taskList);
        final Map<String, ProcessInstance> instances = ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(ids);
        return new Mapcar<Task, ProcessInstance>(taskList) {

            @Override
            public ProcessInstance lambda(Task task) {
                ProcessInstance pi = instances.get(task.getProcessInstanceId());
                if (pi == null) {
                    log.warning("process " + task.getProcessInstanceId() + " not found");
                    return null;
                }
                pi.setState(task.getName());
                pi.setTaskId(task.getId());
                return pi;
            }
        }.go();


    }

    @Override
    public ProcessInstance assignTaskFromQueue(ProcessQueue q, ProcessToolContext processToolContextFromThread) {
        return assignTaskFromQueue(q, null, processToolContextFromThread);
    }

    public List<String> getOutgoingTransitionNames(String internalId, ProcessToolContext ctx) {
        ProcessEngine engine = getProcessEngine();
        org.activiti.engine.runtime.ProcessInstance pi = 
                engine.getRuntimeService().createProcessInstanceQuery().processInstanceId(internalId).singleResult();
        ExecutionImpl execution = (ExecutionImpl) pi;
        List<String> transitionNames = new ArrayList<String>();
        for (PvmTransition transition : execution.getActivity().getOutgoingTransitions()) {
            transitionNames.add(transition.getDestination().getId());
        }
        return transitionNames;
    }

    protected ProcessEngine getProcessEngine() {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if (ctx instanceof ActivitiContextImpl) {
            ProcessEngine engine = ((ActivitiContextImpl) ctx).getProcessEngine();
            if (user != null && user.getLogin() != null)
                engine.getIdentityService().setAuthenticatedUserId(user.getLogin());
            return engine;
        } else {
            throw new IllegalArgumentException(ctx + " not an instance of " + ActivitiContextImpl.class.getName());
        }
    }

    public Collection<ProcessInstance> getUserProcesses(int offset, int limit, ProcessToolContext ctx) {
        List<Task> taskList = getProcessEngine().getTaskService().createTaskQuery()
                .taskAssignee(user.getLogin())
                .orderByExecutionId()
                .desc()
                .listPage(offset, limit);

        List<String> ids = keyFilter("processInstanceId", taskList);
        final Map<String, ProcessInstance> instances = ctx.getProcessInstanceDAO().getProcessInstanceByInternalIdMap(ids);
        return new Mapcar<Task, ProcessInstance>(taskList) {

            @Override
            public ProcessInstance lambda(Task task) {
                ProcessInstance pi = instances.get(task.getProcessInstanceId());
                if (pi == null) {
                    log.warning("process " + task.getProcessInstanceId() + " not found");
                    return null;
                }
                pi.setState(task.getName());
                pi.setTaskId(task.getId());
                return pi;
            }
        }.go();
    }

    @Override
    public Collection<ProcessQueue> getUserAvailableQueues(ProcessToolContext ctx) {
        Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
        final List<String> names = keyFilter("name", configs);

        if (names.isEmpty()) {
            return new ArrayList();
        }

        TaskService ts = getProcessEngine().getTaskService();
        for (ProcessQueue q : configs) {
            q.setProcessCount(ts.createTaskQuery().taskCandidateGroup(q.getName()).taskUnnassigned().count());
        }
        return configs;
    }

    @Override
    public ProcessInstance assignTaskFromQueue(final ProcessQueue pq, ProcessInstance pi, ProcessToolContext ctx) {

        Collection<ProcessQueue> configs = getUserQueuesFromConfig(ctx);
        final List<String> names = keyFilter("name", configs);
        final String taskId = pi != null ? pi.getTaskId() : null;
        if (!names.contains(pq.getName())) throw new ProcessToolSecurityException("queue.no.rights", pq.getName());
        TaskService ts = getProcessEngine().getTaskService();

        Task task;
        if (taskId == null) {
            task = ts.createTaskQuery()
                    .taskCandidateGroup(pq.getName())
                    .taskUnnassigned()
                    .orderByExecutionId()
                    .desc()
                    .singleResult();
        } else {
            task = ts.createTaskQuery().taskId(taskId).taskCandidateGroup(pq.getName())
                                .taskUnnassigned().singleResult();
        }
        if (task == null) {
            return null;
        }
        ts.setAssignee(task.getId(), user.getLogin());
       
        ProcessInstance pi2 = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(task.getProcessInstanceId());
        if (pi2 == null) {
            if (pi == null)
                return assignTaskFromQueue(pq, ctx);
            else
                return null;
        }
        if (!user.getLogin().equals(ts.createTaskQuery().taskId(task.getId()).singleResult().getAssignee())) {
            if (pi == null)
                return assignTaskFromQueue(pq, ctx);
            else
                return null;
        }
        pi2.setTaskId(task.getId());
        pi2.setState(task.getName());

        ProcessInstanceLog log = new ProcessInstanceLog();
        log.setLogType(ProcessInstanceLog.LOG_TYPE_CLAIM_PROCESS);
        log.setState(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(pi));
        log.setEntryDate(Calendar.getInstance());
        log.setEventI18NKey("process.log.process-assigned");
        log.setLogValue(pq.getName());
        log.setUser(ctx.getProcessInstanceDAO().findOrCreateUser(user));
        log.setAdditionalInfo(pq.getDescription());
        pi2.addProcessLog(log);

        fillProcessAssignmentData(pi2, ctx);
        ctx.getProcessInstanceDAO().saveProcessInstance(pi2);

        eventBusManager.publish(new BpmEvent(BpmEvent.Type.ASSIGN_PROCESS,
                pi2, user));

        ctx.getEventBusManager().publish(new BpmEvent(BpmEvent.Type.ASSIGN_PROCESS,
                pi2,
                user));

        return pi2;
    }

    private void fillProcessAssignmentData(final ProcessInstance pi, ProcessToolContext ctx) {
        Set<String> assignees = new HashSet<String>();
        Set<String> queues = new HashSet<String>();
        TaskService taskService = getProcessEngine().getTaskService();
        for (Task t : findProcessTasks(pi, ctx, false)) {
            if (t.getAssignee() != null) {
                assignees.add(t.getAssignee());
            } else { //some optimization could be possible
                for (IdentityLink participation : taskService.getIdentityLinksForTask(t.getId())) {
                    if ("candidate".equals(participation.getType())) {
                        queues.add(participation.getGroupId());
                    }
                }
            }
        }
        pi.setAssignees(assignees.toArray(new String[assignees.size()]));
        pi.setTaskQueues(queues.toArray(new String[queues.size()]));
    }

    @Override()
    public Collection<BpmTask> getTaskList(ProcessInstance pi, final ProcessToolContext ctx) {
        return getTaskList(pi, ctx, true);
    }

    @Override()
    public Collection<BpmTask> getTaskList(ProcessInstance pi, final ProcessToolContext ctx, final boolean mustHaveAssignee) {
        return new Mapcar<Task, BpmTask>(findProcessTasks(pi, ctx, mustHaveAssignee)) {
            @Override
            public BpmTask lambda(Task x) {
                BpmTask t = new BpmTask();
                t.setOwner(ctx.getUserDataDAO().loadUserByLogin(x.getAssignee()));
                t.setTaskName(x.getName());
                t.setInternalTaskId(x.getId());
                return t;
            }
        }.go();
    }

    @Override
    public boolean isProcessOwnedByUser(final ProcessInstance processInstance, ProcessToolContext ctx) {
        List<Task> taskList = findUserTask(processInstance, ctx);
        return !taskList.isEmpty();

    }

    private List<Task> findUserTask(final ProcessInstance processInstance, ProcessToolContext ctx) {
        return getProcessEngine().getTaskService().createTaskQuery().processInstanceId(processInstance.getInternalId())
                .taskAssignee(user.getLogin()).list();        
    }

    private List<Task> findProcessTasks(final ProcessInstance processInstance, ProcessToolContext ctx) {
        return findProcessTasks(processInstance, ctx, true);
    }

    private List<Task> findProcessTasks(final ProcessInstance processInstance, ProcessToolContext ctx,
                                        final boolean mustHaveAssignee) {
        List<Task> list = getProcessEngine().getTaskService().createTaskQuery()
                .processInstanceId(processInstance.getInternalId()).list();
        if (!mustHaveAssignee)
            return list;

        List<Task> res = new ArrayList<Task>();
        for (Task t : list) {
            if (t.getAssignee() != null) res.add(t);
        }

        return res;
    }

    private Task findProcessTask(final ProcessInstance processInstance, ProcessToolContext ctx) {
        return getProcessEngine().getTaskService().createTaskQuery()
                        .processInstanceId(processInstance.getInternalId()).singleResult();
    }


    public ProcessInstance performAction(ProcessStateAction action,
                                         ProcessInstance processInstance,
                                         ProcessToolContext ctx) {
        List<Task> tasks = findUserTask(processInstance, ctx);

        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("process.not.owner");
        }

        Task task = tasks.get(0);
        return performAction(action, processInstance, ctx, task);
    }

    public ProcessInstance performAction(ProcessStateAction action,
                                         ProcessInstance processInstance,
                                         ProcessToolContext ctx,
                                         BpmTask bpmTask) {
        return performAction(action, processInstance, ctx, getProcessEngine().getTaskService().createTaskQuery().taskId(bpmTask.getInternalTaskId()).singleResult());
    }


    private ProcessInstance performAction(ProcessStateAction action, ProcessInstance processInstance, ProcessToolContext ctx, Task task) {
        processInstance = getProcessData(processInstance.getInternalId(), ctx);

        addActionLogEntry(action, processInstance, ctx);
        ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
        ProcessEngine processEngine = getProcessEngine();

        Map<String,Object> variables = new HashMap<String, Object>();
        variables.put("ACTION",action.getBpmName());
        processEngine.getTaskService().complete(task.getId(), variables);

        String s = getProcessState(processInstance, ctx);
        fillProcessAssignmentData(processInstance, ctx);
        processInstance.setState(s);
        if (s == null && processInstance.getRunning() && !isProcessRunning(processInstance.getInternalId(), ctx)) {
            processInstance.setRunning(false);
        }
        ctx.getProcessInstanceDAO().saveProcessInstance(processInstance);
        publishEvents(processInstance, processInstance.getRunning() ? BpmEvent.Type.SIGNAL_PROCESS : BpmEvent.Type.END_PROCESS);

        return processInstance;
    }

    private void publishEvents(ProcessInstance processInstance, BpmEvent.Type signalProcess) {
        eventBusManager.publish(new BpmEvent(signalProcess,
                processInstance,
                user));
        ProcessToolContext.Util.getThreadProcessToolContext().getEventBusManager().publish(new BpmEvent(signalProcess,
                processInstance,
                user));
    }

    private void addActionLogEntry(ProcessStateAction action, ProcessInstance processInstance, ProcessToolContext ctx) {
        ProcessStateConfiguration state = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(processInstance);

        ProcessInstanceLog log = new ProcessInstanceLog();
        log.setLogType(ProcessInstanceLog.LOG_TYPE_PERFORM_ACTION);
        log.setState(state);
        log.setEntryDate(Calendar.getInstance());
        log.setEventI18NKey("process.log.action-performed");
        log.setLogValue(action.getBpmName());
        log.setAdditionalInfo(nvl(action.getLabel(), action.getDescription(), action.getBpmName()));
        log.setUser(ctx.getProcessInstanceDAO().findOrCreateUser(user));
        log.setUserSubstitute(substitutingUser != null ? ctx.getProcessInstanceDAO().findOrCreateUser(substitutingUser) : null);
        processInstance.addProcessLog(log);
    }

    @Override
    public boolean isProcessRunning(String internalId, ProcessToolContext ctx) {

        if (internalId == null) return false;

        RuntimeService service = getProcessEngine().getRuntimeService();
        org.activiti.engine.runtime.ProcessInstance processInstance = service.createProcessInstanceQuery().processInstanceId(internalId).singleResult();
        return processInstance != null && !processInstance.isEnded();

    }

    protected ProcessInstance startProcessInstance(ProcessDefinitionConfig config,
                                                   String externalKey,
                                                   ProcessToolContext ctx,
                                                   ProcessInstance pi) {
        ProcessEngine processEngine = getProcessEngine();
        final RuntimeService execService = processEngine.getRuntimeService();
        Map vars = new HashMap();
        vars.put("processInstanceId", String.valueOf(pi.getId()));
        vars.put("initiator", user.getLogin());
        for (ProcessInstanceAttribute pia : pi.getProcessAttributes()) {
            if (pia instanceof BpmVariable) {
                BpmVariable bpmVar = (BpmVariable) pia;
                if (hasText(bpmVar.getBpmVariableName())) {
                    vars.put(bpmVar.getBpmVariableName(), bpmVar.getBpmVariableValue());
                }
            }
        }

        org.activiti.engine.runtime.ProcessInstance instance = execService
                .startProcessInstanceByKey(config.getBpmDefinitionKey(),
                                            externalKey,
                                            vars);
        pi.setInternalId(instance.getId());
        fillProcessAssignmentData(pi, ctx);
        return pi;
    }

    @Override
    public ProcessToolBpmSession createSession(UserData user, Collection<String> roleNames, ProcessToolContext ctx) {
        ActivitiBpmSession session = new ActivitiBpmSession(user, roleNames);
        session.substitutingUser = this.user;
        return session;
    }

    @Override
    public void adminCancelProcessInstance(ProcessInstance pi) {
        log.severe("User: " + user.getLogin() + " attempting to cancel process: " + pi.getInternalId());
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        pi = getProcessData(pi.getInternalId(), ctx);
        ProcessEngine processEngine = getProcessEngine();
        processEngine.getRuntimeService().deleteProcessInstance(pi.getInternalId(), "admin-cancelled");
        fillProcessAssignmentData(pi, ctx);
        pi.setRunning(false);
        pi.setState(null);
        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        log.severe("User: " + user.getLogin() + " has cancelled process: " + pi.getInternalId());

    }

    @Override
    public void adminReassignProcessTask(ProcessInstance pi, BpmTask bpmTask, String userLogin) {
        log.severe("User: " + user.getLogin() + " attempting to reassign task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to user: " + userLogin);

        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        pi = getProcessData(pi.getInternalId(), ctx);
        ProcessEngine processEngine = getProcessEngine();
        TaskService ts = processEngine.getTaskService();
        Task task = ts.createTaskQuery().taskId(bpmTask.getInternalTaskId()).singleResult();
        if (nvl(userLogin, "").equals(nvl(task.getAssignee(), ""))) {
            log.severe("User: " + user.getLogin() + " has not reassigned task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " as the user is the same: " + userLogin);
            return;
        }
        //this call should also take care of swimlanes
        ts.setAssignee(bpmTask.getInternalTaskId(), userLogin);
        fillProcessAssignmentData(pi, ctx);
        log.info("Process.running:" + pi.getRunning());
        ctx.getProcessInstanceDAO().saveProcessInstance(pi);
        log.severe("User: " + user.getLogin() + " has reassigned task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to user: " + userLogin);

    }

    @Override
    public void adminCompleteTask(ProcessInstance pi, BpmTask bpmTask, ProcessStateAction action) {
        log.severe("User: " + user.getLogin() + " attempting to complete task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to outcome: " + action);
        performAction(action, pi, ProcessToolContext.Util.getThreadProcessToolContext(), bpmTask);
        log.severe("User: " + user.getLogin() + " has completed task " + bpmTask.getInternalTaskId() + " for process: " + pi.getInternalId() + " to outcome: " + action);

    }

    public List<String> getAvailableLogins(final String filter) {
        IdentityService is = getProcessEngine().getIdentityService();
        List<User> users = is.createUserQuery().userFirstNameLike(filter).list();
        List<String> res = new ArrayList<String>();
        for (User u : users) {
            res.add(u.getId());
        }
        Collections.sort(res);
        return res;

    }

    @Override
    public List<GraphElement> getProcessHistory(final ProcessInstance pi) {
        ProcessEngine processEngine = getProcessEngine();
        HistoryService service = processEngine.getHistoryService();
        HistoricActivityInstanceQuery activityInstanceQuery = service.createHistoricActivityInstanceQuery().processInstanceId(pi.getInternalId());
        List<HistoricActivityInstance> list = activityInstanceQuery.list();

        Map<String, GraphElement> processGraphElements = parseProcessDefinition(pi);

        ArrayList<GraphElement> res = new ArrayList<GraphElement>();
        for (HistoricActivityInstance activity : list) {
            LOGGER.fine("Handling: " + activity.getActivityName());
            String activityName = activity.getActivityName();
            if (res.isEmpty()) { //initialize start node and its transition
                GraphElement startNode = processGraphElements.get("__AWF__start_node");
                if (startNode != null) {
                    res.add(startNode);
                }
                GraphElement firstTransition = processGraphElements.get("__AWF__start_transition_to_" + activityName);
                if (firstTransition != null) {
                    res.add(firstTransition);
                }
            }
            StateNode sn = (StateNode) processGraphElements.get(activityName);
            if (sn == null) continue;
            sn = sn.cloneNode();
            sn.setUnfinished(activity.getEndTime() == null);
            sn.setLabel(activityName + ": " + activity.getDurationInMillis() + "ms");
            res.add(sn);
            //look for transition
            TransitionArc ta = (TransitionArc) processGraphElements.get(activityName + "_" + "TODO");
            if (ta == null) { //look for default!
                ta = (TransitionArc) processGraphElements.get("__AWF__default_transition_" + activityName);
            }
            if (ta == null) {
                continue;
            }
            res.add(ta.cloneNode());
        }
        HistoricProcessInstanceQuery historyProcessInstanceQuery = getProcessEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceId(pi.getInternalId());
        HistoricProcessInstance historyProcessInstance = historyProcessInstanceQuery.singleResult();
        if (historyProcessInstance != null && historyProcessInstance.getEndActivityId() != null) {
            StateNode sn = (StateNode) processGraphElements.get(historyProcessInstance.getEndActivityId());
            if (sn != null) {
                StateNode e = sn.cloneNode();
                e.setUnfinished(true);
                res.add(e);
            }
        }
        return res;
    }

    private HashMap<String, GraphElement> parseProcessDefinition(ProcessInstance pi) {
        HashMap<String, GraphElement> res = new HashMap<String, GraphElement>();
        byte[] processDefinition = getProcessDefinition(pi);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(new ByteArrayInputStream(processDefinition));
            Element documentElement = dom.getDocumentElement();
            String[] nodeTypes = new String[]{"start", "end", "java", "task", "decision"};
            for (String nodeType : nodeTypes) {
                NodeList nodes = documentElement.getElementsByTagName(nodeType);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    try {
                        StateNode sn = new StateNode();
                        String gval = node.getAttribute("g");
                        String[] vals = gval.split(",", 4);
                        int x = Integer.parseInt(vals[0]);
                        int y = Integer.parseInt(vals[1]);
                        int w = Integer.parseInt(vals[2]);
                        int h = Integer.parseInt(vals[3]);
                        sn.setX(x);
                        sn.setY(y);
                        sn.setWidth(w);
                        sn.setHeight(h);
                        String name = node.getAttribute("name");
                        sn.setLabel(name);
                        res.put(name, sn);
                        if ("start".equals(nodeType)) {
                            res.put("__AWF__start_node", sn);
                        }
                        LOGGER.fine("Found node" + name + ": " + x + "," + y + "," + w + "," + h);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
            //once again - for transitions
            for (String nodeType : nodeTypes) {
                NodeList nodes = documentElement.getElementsByTagName(nodeType);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    try {
                        String startNodeName = node.getAttribute("name");
                        StateNode startNode = (StateNode) res.get(startNodeName);
                        if (startNode == null) {
                            LOGGER.severe("Start node " + startNodeName +
                                    " has not been localized, skipping transition drawing too.");
                            continue;
                        }
                        NodeList transitions = node.getElementsByTagName("transition");
                        for (int j = 0; j < transitions.getLength(); j++) {
                            Element transitionEl = (Element) transitions.item(j);
                            String name = transitionEl.getAttribute("name");
                            String to = transitionEl.getAttribute("to");
                            StateNode endNode = (StateNode) res.get(to);
                            if (endNode == null) {
                                LOGGER.severe("End node " + to + " has not been localized for transition " + name +
                                        " of node " + startNodeName + ", skipping transition drawing.");
                                continue;
                            }
                            String g = transitionEl.getAttribute("g");
                            if (g != null) {
                                String[] dockersAndDistances = g.split(":");
                                String[] dockers = new String[0];
                                if (dockersAndDistances.length == 2) {
                                    dockers = dockersAndDistances[0].split(";");//what the other numbers mean - I have no idea...
                                }
                                //calculate line start node which is a center of the start node
                                int startX = startNode.getX() + startNode.getWidth() / 2;
                                int startY = startNode.getY() + startNode.getHeight() / 2;
                                //and the same for end node
                                int endX = endNode.getX() + endNode.getWidth() / 2;
                                int endY = endNode.getY() + endNode.getHeight() / 2;

                                TransitionArc arc = new TransitionArc();
                                arc.setName(name);
                                arc.addPoint(startX, startY);
                                for (String docker : dockers) {
                                    String[] split = docker.split(",", 2);
                                    arc.addPoint(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                                }
                                arc.addPoint(endX, endY);

                                double a;//remember about vertical line
                                double b;

                                endX = arc.getPath().get(1).getX();
                                endY = arc.getPath().get(1).getY();
                                if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
                                    if (endY > startNode.getY() + startNode.getHeight()) { //below
                                        startY = startNode.getY() + startNode.getHeight();
                                    } else {
                                        startY = startNode.getY();
                                    }
                                } else {
                                    a = ((double) (startY - endY)) / ((double) (startX - endX));
                                    b = (double) startY - (double) startX * a;
                                    for (int x = startX; x <= endX; x++) {
                                        int y = (int) Math.round(a * x + b);
                                        boolean inside = false;
                                        if (x >= startNode.getX() && x <= startNode.getX() + startNode.getWidth()) {
                                            if (y >= startNode.getY() && y <= startNode.getY() + startNode.getHeight()) {
                                                inside = true;
                                            }
                                        }
                                        if (!inside) {
                                            startX = x;
                                            startY = y;
                                            break;
                                        }
                                    }
                                    for (int x = startX; x > endX; x--) {
                                        int y = (int) Math.round(a * x + b);
                                        boolean inside = false;
                                        if (x >= startNode.getX() && x <= startNode.getX() + startNode.getWidth()) {
                                            if (y >= startNode.getY() && y <= startNode.getY() + startNode.getHeight()) {
                                                inside = true;
                                            }
                                        }
                                        if (!inside) {
                                            startX = x;
                                            startY = y;
                                            break;
                                        }
                                    }
                                }
                                arc.getPath().get(0).setX(startX);
                                arc.getPath().get(0).setY(startY);

                                endX = arc.getPath().get(arc.getPath().size() - 1).getX();
                                endY = arc.getPath().get(arc.getPath().size() - 1).getY();
                                startX = arc.getPath().get(arc.getPath().size() - 2).getX();
                                startY = arc.getPath().get(arc.getPath().size() - 2).getY();
                                if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
                                    if (startY > endNode.getY() + endNode.getHeight()) { //below
                                        endY = endNode.getY() + endNode.getHeight();
                                    } else {
                                        endY = endNode.getY();
                                    }
                                } else {
                                    a = ((double) (startY - endY)) / ((double) (startX - endX));//remember about vertical line
                                    //startY = startX*a+b
                                    b = (double) startY - (double) startX * a;
                                    for (int x = endX; x <= startX; x++) {
                                        int y = (int) Math.round(a * x + b);
                                        boolean inside = false;
                                        if (x >= endNode.getX() && x <= endNode.getX() + endNode.getWidth()) {
                                            if (y >= endNode.getY() && y <= endNode.getY() + endNode.getHeight()) {
                                                inside = true;
                                            }
                                        }
                                        if (!inside) {
                                            endX = x;
                                            endY = y;
                                            break;
                                        }
                                    }
                                    for (int x = endX; x > startX; x--) {
                                        int y = (int) Math.round(a * x + b);
                                        boolean inside = false;
                                        if (x >= endNode.getX() && x <= endNode.getX() + endNode.getWidth()) {
                                            if (y >= endNode.getY() && y <= endNode.getY() + endNode.getHeight()) {
                                                inside = true;
                                            }
                                        }
                                        if (!inside) {
                                            endX = x;
                                            endY = y;
                                            break;
                                        }
                                    }
                                }
                                arc.getPath().get(arc.getPath().size() - 1).setX(endX);
                                arc.getPath().get(arc.getPath().size() - 1).setY(endY);

                                res.put(startNodeName + "_" + name, arc);
                                if ("start".equals(nodeType)) {
                                    res.put("__AWF__start_transition_to_" + to, arc);
                                }
                                if (transitions.getLength() == 1) {
                                    res.put("__AWF__default_transition_" + startNodeName, arc);
                                }
                            } else {
                                LOGGER.severe("No 'g' attribute for transition " + name +
                                        " of node " + startNodeName + ", skipping transition drawing.");
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    @Override
    public byte[] getProcessLatestDefinition(String definitionKey, String processName) {
        String resourceName = processName + ".bpmn20.xml";
        return fetchLatestProcessResource(definitionKey, resourceName);
    }

    @Override
    public byte[] getProcessMapImage(ProcessInstance pi) {
        String resourceName = pi.getDefinition().getProcessName() + ".png";
        return fetchProcessResource(pi, resourceName);
    }

    @Override
    public byte[] getProcessDefinition(ProcessInstance pi) {
        String resourceName = pi.getDefinition().getProcessName() + ".bpmn20.xml";
        return fetchProcessResource(pi, resourceName);
    }

    private byte[] fetchLatestProcessResource(String definitionKey, String resourceName) {
        RepositoryService service = getProcessEngine()
                .getRepositoryService();
        List<ProcessDefinition> latestList = service.createProcessDefinitionQuery()
                .processDefinitionKey(definitionKey).
                        orderByDeploymentId().desc().listPage(0, 1);
        if (!latestList.isEmpty()) {
            String oldDeploymentId = latestList.get(0).getDeploymentId();
            return getDeploymentResource(resourceName, oldDeploymentId);
        }
        return null;
    }

    private byte[] fetchProcessResource(ProcessInstance pi, String resourceName) {
        ProcessEngine processEngine = getProcessEngine();
        RepositoryService service = processEngine.getRepositoryService();

        RuntimeService RuntimeService = processEngine.getRuntimeService();
        org.activiti.engine.runtime.ProcessInstance processInstanceById =
                RuntimeService.createProcessInstanceQuery().processInstanceId(pi.getInternalId()).singleResult();
        String processDefinitionId;
        if (processInstanceById == null) { //look in history service
            HistoricProcessInstanceQuery historyProcessInstanceQuery = processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery().processInstanceId(pi.getInternalId());
            HistoricProcessInstance historyProcessInstance = historyProcessInstanceQuery.singleResult();
            processDefinitionId = historyProcessInstance.getProcessDefinitionId();
        } else {
            processDefinitionId = processInstanceById.getProcessDefinitionId();
        }
        List<ProcessDefinition> latestList = service.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).orderByDeploymentId().desc().listPage(0, 1);
        if (!latestList.isEmpty()) {
            String oldDeploymentId = latestList.get(0).getDeploymentId();
            return getDeploymentResource(resourceName, oldDeploymentId);
        }
        return null;
    }

    private byte[] getDeploymentResource(String resourceName, String oldDeploymentId) {
        RepositoryService service = getProcessEngine().getRepositoryService();
        ;
        try {
            InputStream oldStream = service.getResourceAsStream(oldDeploymentId, resourceName);
            try {
                if (oldStream != null) {
                    ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                    int c;
                    while ((c = oldStream.read()) >= 0) {
                        bos2.write(c);
                    }
                    return bos2.toByteArray();
                }
            } finally {
                if (oldStream != null) {
                    oldStream.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String deployProcessDefinition(String processName, InputStream definitionStream, InputStream processMapImageStream) {
        RepositoryService service = getProcessEngine()
                .getRepositoryService();
        DeploymentBuilder deployment = service.createDeployment();
//        deployment.name(processName);
        deployment.addInputStream(processName + ".bpmn20.xml", definitionStream);
        if (processMapImageStream != null)
            deployment.addInputStream(processName + ".png", processMapImageStream);
        Deployment deploy = deployment.deploy();
        return deploy.getId();
    }
}
