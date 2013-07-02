package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import org.jbpm.task.identity.UserGroupCallback;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.util.lang.ExpiringCache;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.rnd.processtool.bpm.impl.AbstractProcessToolSession.getQueuesFromConfig;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2013-06-14
 * Time: 13:53
 */
public class AwfUserCallback implements UserGroupCallback {
	private static final String DUMMY_USER_GROUP = "__DUMMY_USER_GROUP__";

	private static final ExpiringCache<String, Set<String>> queueNamesByUserLogin = new ExpiringCache<String, Set<String>>(60 * 60 * 1000);

	@Override
	public boolean existsUser(String userId) {
		// accept all by default
		return true;
	}

	@Override
	public boolean existsGroup(String groupId) {
		// accept all by default
		return true;
	}

	@Override
	public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
		List<String> result;
		if(groupIds != null) {
			result = new ArrayList<String>(groupIds);
			// merge all groups
			if(allExistingGroupIds != null) {
				for(String grp : allExistingGroupIds) {
					if(!result.contains(grp)) {
						result.add(grp);
					}
				}
			}
		}
		else {
			result = new ArrayList<String>(getGroupIdsForUser(userId));
		}
		// there is a bug that causes some task queries to fail on empty list (because of empty 'in' clause)
		if (result.isEmpty()) {
			result.add(DUMMY_USER_GROUP);
		}
		return result;
	}

	private Set<String> getGroupIdsForUser(final String userId) {
		return queueNamesByUserLogin.get(userId, new ExpiringCache.NewValueCallback<String, Set<String>>() {
			@Override
			public Set<String> getNewValue(String key) {
				UserData user = getThreadProcessToolContext().getUserDataDAO().loadUserByLogin(userId);

				if (user == null) {
					return Collections.emptySet();
				}

				List<ProcessQueue> queues = getQueuesFromConfig(user.getRoleNames());

				return from(queues).select(GET_NAME).toSet();
			}
		});
	}

	private static final F<ProcessQueue,String> GET_NAME = new F<ProcessQueue, String>() {
		@Override
		public String invoke(ProcessQueue x) {
			return x.getName();
		}
	};
}
