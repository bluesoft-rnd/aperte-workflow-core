package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolJbpmSessionFactory implements ProcessToolSessionFactory {
	private UserData autoUser;

	private ProcessToolContext ctx;

	public ProcessToolJbpmSessionFactory(ProcessToolContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public ProcessToolBpmSession createSession(UserData user, Collection<String> roles) {
		return new ProcessToolJbpmSession(user, roles, ctx);
	}

	@Override
	public ProcessToolBpmSession createAutoSession() {
		return createAutoSession(new HashSet<String>());
	}

	@Override
	public ProcessToolBpmSession createAutoSession(Collection<String> roles) {
		if (autoUser == null) {
			autoUser = ctx.getAutoUser();
		}
		return new ProcessToolJbpmSession(autoUser, roles, ctx);
	}
}
