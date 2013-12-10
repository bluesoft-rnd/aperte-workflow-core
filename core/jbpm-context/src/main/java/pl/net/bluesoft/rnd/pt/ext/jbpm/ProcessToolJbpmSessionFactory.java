package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.Collections;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Component
@Scope(value = "singleton")
public class ProcessToolJbpmSessionFactory implements ProcessToolSessionFactory
{

    public ProcessToolJbpmSessionFactory()
    {

    }

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
