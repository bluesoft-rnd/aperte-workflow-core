package org.aperteworkflow.ext.activiti;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ActivitiBpmSessionFactory implements ProcessToolSessionFactory {

	public ActivitiBpmSessionFactory() {
	}

	public ProcessToolBpmSession createSession(UserData user, Collection<String> roles) {
		ActivitiBpmSession sess = new ActivitiBpmSession(user, roles);
		return sess;
	}
}
