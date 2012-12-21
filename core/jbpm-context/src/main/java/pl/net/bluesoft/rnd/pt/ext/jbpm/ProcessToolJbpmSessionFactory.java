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

	public ProcessToolBpmSession createSession(UserData user, Collection<String> roles) {
		ProcessToolJbpmSession processToolJbpmSession = new ProcessToolJbpmSession(user, roles, ctx);
		return processToolJbpmSession;
	}

    public ProcessToolBpmSession createAutoSession() {
           return createAutoSession(new HashSet<String>());
       }

   	public ProcessToolBpmSession createAutoSession(Collection<String> roles) {
   		if (autoUser == null) {
   			autoUser = ctx.getAutoUser();
   		}
   		return new ProcessToolJbpmSession(autoUser, roles, ctx);
   	}
}
