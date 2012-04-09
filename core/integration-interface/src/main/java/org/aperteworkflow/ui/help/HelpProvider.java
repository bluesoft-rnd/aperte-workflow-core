package org.aperteworkflow.ui.help;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface HelpProvider {

    void prepare(Application application, ProcessDefinitionConfig cfg);
    Component helpIcon(String taskName, String s);
}
