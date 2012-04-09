package org.aperteworkflow.ui.help;

import com.vaadin.Application;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface HelpProviderFactory {
    HelpProvider getInstance(Application application, ProcessDefinitionConfig cfg);
}
