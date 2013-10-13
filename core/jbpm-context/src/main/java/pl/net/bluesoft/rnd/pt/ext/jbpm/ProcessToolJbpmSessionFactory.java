package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.Collections;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolJbpmSessionFactory implements ProcessToolSessionFactory {
	@Override
	public String getBpmDefinitionLanguage() {
		return "bpmn20";
	}

	@Override
	public ProcessToolBpmSession createSession(UserData user) {
		return createSession(user.getLogin(), user.getRoles());
	}

	@Override
	public ProcessToolBpmSession createSession(String userLogin) {
		return createSession(userLogin, null);
	}

	@Override
	public ProcessToolBpmSession createSession(String userLogin, Collection<String> roles) {
		return new ProcessToolJbpmSession(userLogin, roles, null);
	}

	@Override
	public ProcessToolBpmSession createAutoSession() {
		return createAutoSessionHelper(Collections.<String>emptySet());
	}

	@Override
	public ProcessToolBpmSession createAutoSession(Collection<String> roles) {
		return createAutoSessionHelper(roles);
	}

	private ProcessToolBpmSession createAutoSessionHelper(Collection<String> roles) {
		return new ProcessToolJbpmSession(ProcessToolBpmConstants.ADMIN_USER.getLogin(), roles, null);
	}
}
