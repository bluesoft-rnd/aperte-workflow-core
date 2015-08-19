package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

import java.util.*;

/**
 * Psuedo task to show post action widget
 */
public class BpmPseudoTask extends BpmTaskDerivedBean {

    public BpmPseudoTask(BpmTask task) {
        super(task);
    }
}
