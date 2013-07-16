package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.Collections;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolJbpmSessionFactory implements ProcessToolSessionFactory {
	private UserData autoUser;

	@Override
	public ProcessToolBpmSession createSession(UserData user) {
		return createSession(user, Collections.<String>emptySet());
	}

	@Override
	public ProcessToolBpmSession createSession(UserData user, Collection<String> roles) {
		return new ProcessToolJbpmSession(user, roles);
	}

	@Override
	public ProcessToolBpmSession createAutoSession() {
		return createAutoSession(getThreadProcessToolContext(), Collections.<String>emptySet());
	}

	@Override
	public ProcessToolBpmSession createAutoSession(ProcessToolContext ctx) {
		return createAutoSession(ctx, Collections.<String>emptySet());
	}

	@Override
	public ProcessToolBpmSession createAutoSession(Collection<String> roles) {
		return createAutoSession(getThreadProcessToolContext(), roles);
	}

	private ProcessToolBpmSession createAutoSession(ProcessToolContext ctx, Collection<String> roles) {
		if (autoUser == null) {
			autoUser = ctx.getAutoUser();
		}
		return new ProcessToolJbpmSession(autoUser, roles);
	}
}
