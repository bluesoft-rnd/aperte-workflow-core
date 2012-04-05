package pl.net.bluesoft.rnd.processtool.ui.tasks;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

public class TaskTableItem {
    private String definitionName, internalId, state, externalId;

    private ProcessInstance processInstance;
    private ProcessStateConfiguration stateConfiguration;

    private BpmTask task;

    public TaskTableItem(BpmTask task) {
        this.task = task;
        init();
    }

    private void init() {
        processInstance = task.getProcessInstance();
        definitionName = processInstance.getDefinitionName();
        internalId = processInstance.getInternalId();
        externalId = processInstance.getExternalKey();
        for (ProcessStateConfiguration st : processInstance.getDefinition().getStates()) {
            if (task.getTaskName().equals(st.getName())) {
                state = st.getDescription();
                stateConfiguration = st;
                break;
            }
        }
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public String getState() {
        return state;
    }

    public String getInternalId() {
        return internalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public BpmTask getTask() {
        return task;
    }

    public ProcessStateConfiguration getStateConfiguration() {
        return stateConfiguration;
    }

    public boolean matchSearchCriteria(String expression) {
        String[] fields = new String[] {
                state,
                internalId,
                externalId,
                definitionName,
                processInstance.getDescription(),
                processInstance.getKeyword(),
                stateConfiguration != null ? stateConfiguration.getCommentary() : null
        };
        for (String f : fields) {
            if (f != null && f.toUpperCase().contains(expression.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
