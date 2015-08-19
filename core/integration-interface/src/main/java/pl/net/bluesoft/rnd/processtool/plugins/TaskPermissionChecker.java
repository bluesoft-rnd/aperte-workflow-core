package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.Set;

/**
 * Created by mpawluczuk on 2015-02-11.
 */
public interface TaskPermissionChecker {
	Boolean hasPermission(UserData user, Set<QueueBean> taskQueues, BpmTask task);
}
