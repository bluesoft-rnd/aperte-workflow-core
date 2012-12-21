package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.util.Date;

public class MutableBpmTask extends BpmTask {
    public MutableBpmTask() {
        super();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public MutableBpmTask(BpmTask task) {
        super(task);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
