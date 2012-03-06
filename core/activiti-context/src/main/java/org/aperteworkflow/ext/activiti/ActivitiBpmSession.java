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
import org.aperteworkflow.bpm.graph.TransitionArcPoint;
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
    public static final String BPMNDI_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/DI";
    public static final String BPMN_NS = "http://www.omg.org/spec/BPMN/20100524/MODEL";
    public static final String OMG_DC_URI = "http://www.omg.org/spec/DD/20100524/DC";

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
            transitionNames.add(transition.getId());
        }
        return transitionNames;
    }

    @Override
    public List<String> getOutgoingTransitionDestinationNames(String internalId, ProcessToolContext ctx) {
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
        User user1 = is.createUserQuery().userId(filter).singleResult();

        List<User> users = is.createUserQuery()
                .userEmailLike("%" + filter + "%")
                .orderByUserId().asc()
                .listPage(0, 100);
        List<String> res = new ArrayList<String>();
        for (User u : users) {
            res.add(u.getId());
        }
        Collections.sort(res);
        if (user1 != null) {
            res.add(0, user1.getId());
        }
        return res;

    }

    @Override
    public List<GraphElement> getProcessHistory(final ProcessInstance pi) {
        ProcessEngine processEngine = getProcessEngine();
        HistoryService service = processEngine.getHistoryService();
        HistoricActivityInstanceQuery activityInstanceQuery = service.createHistoricActivityInstanceQuery().processInstanceId(pi.getInternalId());
        List<HistoricActivityInstance> list = activityInstanceQuery.list();

        Collections.sort(list, new Comparator<HistoricActivityInstance>() {
            @Override
            public int compare(HistoricActivityInstance o1, HistoricActivityInstance o2) {
                return nvl(new Date(),o1.getStartTime()).compareTo(nvl(new Date(),o2.getStartTime()));
            }
        });

        Map<String, GraphElement> processGraphElements = parseProcessDefinition(pi);

        ArrayList<GraphElement> res = new ArrayList<GraphElement>();
        HistoricActivityInstance prev = null;
        for (HistoricActivityInstance activity : list) {
            LOGGER.fine("Handling: " + activity.getActivityName());
            String activityName = activity.getActivityName();
            if (prev != null) {
                TransitionArc ta = (TransitionArc) processGraphElements.get("__AWF__" + prev.getActivityName() + "_" + activityName);
                if (ta == null) { //look for default!
                    ta = (TransitionArc) processGraphElements.get("__AWF__default_transition_" + prev.getActivityName());
                }
                if (ta != null) {
                    res.add(ta.cloneNode());

                }
            }
            prev = activity;

            //activiti notes first event quite well
//            if (res.isEmpty()) { //initialize start node and its transition
//                GraphElement startNode = processGraphElements.get("__AWF__start_node");
//                if (startNode != null) {
//                    res.add(startNode);
//                }
//                GraphElement firstTransition = processGraphElements.get("__AWF__start_transition_to_" + activityName);
//                if (firstTransition != null) {
//                    res.add(firstTransition);
//                }
//            }
            StateNode sn = (StateNode) processGraphElements.get(activityName);
            if (sn == null) continue;
            sn = sn.cloneNode();
            sn.setUnfinished(activity.getEndTime() == null);
//            sn.setLabel(activityName + ": " + activity.getDurationInMillis() + "ms");
            res.add(sn);
            //look for transition
        }

        HistoricProcessInstanceQuery historyProcessInstanceQuery = getProcessEngine().getHistoryService()
                .createHistoricProcessInstanceQuery().processInstanceId(pi.getInternalId());
        HistoricProcessInstance historyProcessInstance = historyProcessInstanceQuery.singleResult();
        if (historyProcessInstance != null && historyProcessInstance.getEndActivityId() != null) {
            StateNode sn = (StateNode) processGraphElements.get(historyProcessInstance.getEndActivityId());
            if (sn != null) {
                if (prev != null) {
                    TransitionArc ta = (TransitionArc) processGraphElements.get("__AWF__" + prev.getActivityName() + "_" + sn.getLabel());
                    if (ta == null) { //look for default!
                        ta = (TransitionArc) processGraphElements.get("__AWF__default_transition_" + prev.getActivityName());
                    }
                    if (ta != null) {
                        res.add(ta.cloneNode());

                    }
                }
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
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            //parse using builder to get DOM representation of the XML file
            Document dom = db.parse(new ByteArrayInputStream(processDefinition));
            Element documentElement = dom.getDocumentElement();

            NodeList diagrams = documentElement.getElementsByTagNameNS(BPMNDI_NAMESPACE,
                    "BPMNDiagram");
            if (diagrams.getLength() == 0) {
                log.severe("No diagram data for process definition for instance " + pi.getInternalId());
                return res;
            }
            Element diagramElement = (Element) diagrams.item(0);
            NodeList planes = diagramElement.getElementsByTagNameNS(BPMNDI_NAMESPACE, "BPMNPlane");
            if (planes.getLength() == 0) {
                log.severe("No plane data for process definition for instance " + pi.getInternalId());
                return res;
            }
            Element planeElement = (Element) planes.item(0);

            Map<String, StateNode> nodeById = getElementCoordinatesMap(planeElement);
            Map<String, TransitionArc> arcById = getArcCoordinatesMap(planeElement);

            NodeList processElements = documentElement.getElementsByTagName("process");
            if (processElements.getLength() == 0) {
                log.severe("No process data for process definition for instance " + pi.getInternalId());
            }
            Element processElement = (Element) processElements.item(0);

            String[] nodeTypes = new String[]{"startEvent", "userTask", "exclusiveGateway", "serviceTask", "endEvent"};
            for (String nodeType : nodeTypes) {
                NodeList nodes = processElement.getElementsByTagName(nodeType);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    try {
                        StateNode sn = nodeById.get(node.getAttribute("id"));
                        if (sn == null) {
                            continue;
                        }
                        String name = node.getAttribute("name");
                        sn.setLabel(name);
                        res.put(name, sn);
                        res.put(sn.getId(), sn);
                        if ("startEvent".equals(nodeType)) {
                            res.put("__AWF__start_node", sn);
                        }
                        LOGGER.fine("Found node" + name);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
            /*
<sequenceFlow id="sid-1D101C48-E36A-4A42-8E95-5C83E9A4CE62" name="Continue" sourceRef="sid-7BAAA2C8-B1B8-44C7-8C4F-D3968FBAD1B9" targetRef="sid-9EB37DC6-A17D-4F6B-AD72-64481593034E">
  <conditionExpression id="sid-754c93dd-7b08-4c0b-b858-848069d98e4d" xsi:type="tFormalExpression">${ACTION=='Continue'}</conditionExpression>
</sequenceFlow>
             */
            nodeTypes = new String[]{"sequenceFlow"};
            for (String nodeType : nodeTypes) {
                NodeList nodes = processElement.getElementsByTagName(nodeType);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element node = (Element) nodes.item(i);
                    try {
                        TransitionArc arc = arcById.get(node.getAttribute("id"));
                        if (arc == null) {
                            continue;
                        }
                        String name = node.getAttribute("name");
                        arc.setName(name);
                        res.put(name, arc);
                        StateNode startNode = nodeById.get(node.getAttribute("sourceRef"));
                        StateNode endNode = nodeById.get(node.getAttribute("targetRef"));
                        if (startNode == null || endNode == null) {
                            continue;
                        }
                                                        //calculate line start node which is a center of the start node
                        int startX = startNode.getX() + startNode.getWidth()/2;
                        int startY = startNode.getY() + startNode.getHeight()/2;
                        //and the same for end node
                        int endX   = endNode.getX() + endNode.getWidth()/2;
                        int endY   = endNode.getY() + endNode.getHeight()/2;

                        arc.getPath().add(0, new TransitionArcPoint(startX, startY));
                        arc.addPoint(endX, endY);

                        double a;//remember about vertical line
                        double b;

                        endX = arc.getPath().get(1).getX();
                        endY = arc.getPath().get(1).getY();
                        if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
                            if (endY > startNode.getY()+startNode.getHeight()) { //below
                                startY = startNode.getY()+startNode.getHeight();
                            } else {
                                startY = startNode.getY();
                            }
                        } else {
                            a = ((double)(startY-endY))/((double)(startX - endX));
                            b = (double)startY - (double)startX*a;
                            for (int x = startX; x <= endX; x++) {
                                int y = (int) Math.round(a*x+b);
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
                                int y = (int) Math.round(a*x+b);
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

                        endX = arc.getPath().get(arc.getPath().size()-1).getX();
                        endY = arc.getPath().get(arc.getPath().size()-1).getY();
                        startX = arc.getPath().get(arc.getPath().size()-2).getX();
                        startY = arc.getPath().get(arc.getPath().size()-2).getY();
                        if (startX - endX == 0) { //whoa - vertical line - simple case, but requires special approach
                           if (startY > endNode.getY()+endNode.getHeight()) { //below
                               endY = endNode.getY()+endNode.getHeight();
                           } else {
                               endY = endNode.getY();
                           }
                        } else {
                            a = ((double)(startY-endY))/((double)(startX - endX));//remember about vertical line
                            //startY = startX*a+b
                            b = (double)startY - (double)startX*a;
                            for (int x = endX; x <= startX; x++) {
                                int y = (int) Math.round(a*x+b);
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
                                int y = (int) Math.round(a*x+b);
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
                        arc.getPath().get(arc.getPath().size()-1).setX(endX);
                        arc.getPath().get(arc.getPath().size()-1).setY(endY);
                        
                        res.put("__AWF__" + startNode.getLabel() + "_" + endNode.getLabel(),
                                arc);
                        res.put("__AWF__default_transition_" + startNode.getLabel(),
                                arc);
                        LOGGER.fine("Found node" + name);
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

    private Map<String, StateNode> getElementCoordinatesMap(Element planeElement) {
        Map<String, StateNode> nodeById = new HashMap<String, StateNode>();
        NodeList nodes = planeElement.getElementsByTagNameNS(BPMNDI_NAMESPACE, "BPMNShape");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            try {
                /*
                <bpmndi:BPMNShape bpmnElement="sid-0085EC77-5E5A-41F2-AB86-1323CDAA63B9" id="sid-0085EC77-5E5A-41F2-AB86-1323CDAA63B9_gui">
                            <omgdc:Bounds height="30.0" width="30.0" x="21.0" y="89.0"/>
                         </bpmndi:BPMNShape>
                 */
                StateNode sn = new StateNode();
                sn.setId(node.getAttribute("bpmnElement"));
                NodeList boundsList = node.getElementsByTagNameNS(OMG_DC_URI, "Bounds");
                if (boundsList.getLength() == 0) {
                    continue;
                }
                Element boundsEl = (Element) boundsList.item(0);

                int x = new Double(boundsEl.getAttribute("x")).intValue();
                int y = new Double(boundsEl.getAttribute("y")).intValue();
                int w = new Double(boundsEl.getAttribute("width")).intValue();
                int h = new Double(boundsEl.getAttribute("height")).intValue();
                sn.setX(x);
                sn.setY(y);
                sn.setWidth(w);
                sn.setHeight(h);
                nodeById.put(sn.getId(), sn);
                LOGGER.fine("Found node" + sn.getId() + ": " + x + "," + y + "," + w + "," + h);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return nodeById;
    }

    private Map<String, TransitionArc> getArcCoordinatesMap(Element planeElement) {
        Map<String, TransitionArc> arcById = new HashMap<String, TransitionArc>();
        NodeList nodes = planeElement.getElementsByTagNameNS(BPMNDI_NAMESPACE, "BPMNEdge");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            try {
                /*
                 <bpmndi:BPMNEdge bpmnElement="sid-581B2A50-275F-4790-BA45-FC2728E3E8F7" id="sid-581B2A50-275F-4790-BA45-FC2728E3E8F7_gui">
                    <omgdi:waypoint x="552.0" y="198.0"/>
                    <omgdi:waypoint x="552.3181702913334" y="60.0"/>
                    <omgdi:waypoint x="274.0" y="60.0"/>
                 </bpmndi:BPMNEdge>
                 */
                TransitionArc ta = new TransitionArc();
                ta.setId(node.getAttribute("bpmnElement"));
                NodeList waypointList = node.getElementsByTagNameNS("http://www.omg.org/spec/DD/20100524/DI", "waypoint");
                if (waypointList.getLength() == 0) {
                    continue;
                }
                for (int j = 1; j < waypointList.getLength()-1; j++) {//skip first and last docker - we have to calculate them manually
                    Element waypointEl = (Element) waypointList.item(j);
                    ta.addPoint(new Double(waypointEl.getAttribute("x")).intValue(),
                            new Double(waypointEl.getAttribute("y")).intValue());
                }
                arcById.put(ta.getId(), ta);
                LOGGER.fine("Found arc" + ta);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return arcById;
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
