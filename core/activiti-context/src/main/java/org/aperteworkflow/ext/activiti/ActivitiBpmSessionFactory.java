package org.aperteworkflow.ext.activiti;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivitiBpmSessionFactory implements ProcessToolSessionFactory {

    UserData autoUser;
    public ActivitiBpmSessionFactory() {
    }

    public ProcessToolBpmSession createSession(UserData user, Collection<String> roles) {
        ActivitiBpmSession sess = new ActivitiBpmSession(user, roles);
        return sess;
    }

    public ProcessToolBpmSession createAutoSession() {
        return createAutoSession(new HashSet<String>());
    }

    public ProcessToolBpmSession createAutoSession(Collection<String> roles) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if (autoUser == null) {
            autoUser = ctx.getAutoUser();
        }
        return new ActivitiBpmSession(autoUser, roles);
    }

}
