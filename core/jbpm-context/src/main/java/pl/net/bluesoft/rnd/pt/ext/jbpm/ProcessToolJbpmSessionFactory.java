package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolJbpmSessionFactory implements ProcessToolSessionFactory {

	private ProcessToolContext ctx;

	public ProcessToolJbpmSessionFactory(ProcessToolContext ctx) {
		this.ctx = ctx;
	}

	public ProcessToolBpmSession createSession(UserData user, Collection<String> roles) {
		ProcessToolJbpmSession processToolJbpmSession = new ProcessToolJbpmSession(user, roles, ctx);
		return processToolJbpmSession;
	}
}
