package org.aperteworkflow.help.impl;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import org.aperteworkflow.ui.help.HelpProvider;
import org.vaadin.jonatan.contexthelp.ContextHelp;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class HelpProviderImpl implements HelpProvider {

    private HelpFactory helpFactory;

    @Override
    public void prepare(Application application, ProcessDefinitionConfig cfg) {
        ContextHelp contextHelp = new ContextHelp();
        application.getMainWindow().getContent().addComponent(contextHelp);
        helpFactory = new HelpFactory(
                cfg,
                application,
                I18NSource.ThreadUtil.getThreadI18nSource(),
                "step_help",
                contextHelp);
    }

    @Override
    public Component helpIcon(String taskName, String s) {
        return helpFactory.helpIcon(taskName, s);
    }
}
