package org.aperteworkflow.webapi.main.processes.processor;

import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

/**
 * Created by pkuciapski on 2014-05-15.
 */
public class TaskProcessor extends AbstractSaveProcessor {
    private BpmTask task;

    public TaskProcessor(BpmTask task, I18NSource messageSource, Collection<HtmlWidget> widgets) {
        super(messageSource, widgets);
        this.task = task;
    }

    @Override
    protected IAttributesProvider getProvider() {
        return task;
    }

    @Override
    protected IAttributesConsumer getConsumer() {
        return task.getProcessInstance();
    }

	@Override
	protected void auditLog(Collection<HandlingResult> results) {
		ProcessInstance process = getProvider().getProcessInstance();

		if (process != null && !results.isEmpty()) {
			String json = null;
			try {
				json = mapper.writeValueAsString(results);
			} catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			ProcessInstanceLog log = new ProcessInstanceLog();
			log.setState(null);
			log.setEntryDate(new Date());
			log.setEventI18NKey("process.log.process-change");
			// todo
			if (getProvider() instanceof BpmTask)
				log.setUserLogin(((BpmTask) getProvider()).getAssignee());
			log.setLogType(ProcessInstanceLog.LOG_TYPE_PROCESS_CHANGE);
			log.setOwnProcessInstance(process);
			log.setLogValue(json);
			process.getRootProcessInstance().addProcessLog(log);
		}
	}
}
