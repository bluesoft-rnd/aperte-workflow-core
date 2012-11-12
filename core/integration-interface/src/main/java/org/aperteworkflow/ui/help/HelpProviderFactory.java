package org.aperteworkflow.ui.help;

import com.vaadin.Application;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.util.List;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface HelpProviderFactory {
	HelpProvider getInstance(Application application, ProcessDefinitionConfig cfg, boolean canEdit, String dictName);
	HelpProvider getInstance(Application application, List<ProcessDefinitionConfig> cfgs, boolean canEdit, String dictName);
}
