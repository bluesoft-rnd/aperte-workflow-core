package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolSessionFactory extends ProcessToolBpmConstants {

	ProcessToolBpmSession createSession(UserData user, Collection<String> roles);

    ProcessToolBpmSession createAutoSession();
	ProcessToolBpmSession createAutoSession(Collection<String> roles);
}
