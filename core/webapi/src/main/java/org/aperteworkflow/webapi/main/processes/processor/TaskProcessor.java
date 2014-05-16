package org.aperteworkflow.webapi.main.processes.processor;

import org.aperteworkflow.webapi.main.processes.domain.HtmlWidget;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;

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
}
