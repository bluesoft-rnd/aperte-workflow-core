package org.aperteworkflow.webapi.main.ui;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.IStateWidget;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.List;

/**
 * Created by pkuciapski on 2014-04-28.
 */
public abstract class AbstractViewBuilder<T extends AbstractViewBuilder> {
    protected List<? extends IStateWidget> widgets;
    protected I18NSource i18Source;
    protected UserData user;
    protected ProcessToolContext ctx;
    protected Collection<String> userQueues;
    protected ProcessToolBpmSession bpmSession;


    protected abstract T getThis();

    public T setWidgets(List<? extends IStateWidget> widgets) {
        this.widgets = widgets;
        return getThis();
    }

    public T setI18Source(I18NSource i18Source) {
        this.i18Source = i18Source;
        return getThis();
    }

    public T setUser(UserData user) {
        this.user = user;
        return getThis();
    }

    public T setCtx(ProcessToolContext ctx) {
        this.ctx = ctx;
        return getThis();
    }

    public T setUserQueues(Collection<String> userQueues) {
        this.userQueues = userQueues;
        return getThis();
    }

    public T setBpmSession(ProcessToolBpmSession bpmSession) {
        this.bpmSession = bpmSession;
        return getThis();
    }

}
