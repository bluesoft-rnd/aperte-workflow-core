package pl.net.bluesoft.rnd.processtool.ui.widgets.taskitem;

import com.vaadin.terminal.Resource;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2011-12-14
 * Time: 14:36:52
 */
public abstract class TaskItemProviderParams {
    private ProcessToolContext ctx;
    private ProcessToolBpmSession bpmSession;
    private I18NSource i18NSource;
    private ProcessInstance processInstance;
    private BpmTask task;
    private ProcessStateConfiguration processStateConfiguration;
    private String state;
    private ProcessQueue queue;
	private boolean replaceDefault = false;

    public abstract Resource getImage(String image);
    public abstract Resource getResource(String path);
    public abstract Resource getStreamResource(String path, byte[] processLogo);

    public abstract void onClick();

    public ProcessToolContext getCtx() {
        return ctx;
    }

    public void setCtx(ProcessToolContext ctx) {
        this.ctx = ctx;
    }

    public ProcessToolBpmSession getBpmSession() {
        return bpmSession;
    }

    public void setBpmSession(ProcessToolBpmSession bpmSession) {
        this.bpmSession = bpmSession;
    }

    public ProcessStateConfiguration getProcessStateConfiguration() {
        return processStateConfiguration;
    }

    public void setProcessStateConfiguration(ProcessStateConfiguration processStateConfiguration) {
        this.processStateConfiguration = processStateConfiguration;
    }

    public I18NSource getI18NSource() {
        return i18NSource;
    }

    public void setI18NSource(I18NSource i18NSource) {
        this.i18NSource = i18NSource;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public BpmTask getTask() {
        return task;
    }

    public void setTask(BpmTask task) {
        this.task = task;
    }

    public String getMessage(String key) {
        return i18NSource.getMessage(key);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ProcessQueue getQueue() {
        return queue;
    }

    public void setQueue(ProcessQueue queue) {
        this.queue = queue;
    }

	public boolean isReplaceDefault() {
		return replaceDefault;
	}

	public void setReplaceDefault(boolean replaceDefault) {
		this.replaceDefault = replaceDefault;
	}
}
