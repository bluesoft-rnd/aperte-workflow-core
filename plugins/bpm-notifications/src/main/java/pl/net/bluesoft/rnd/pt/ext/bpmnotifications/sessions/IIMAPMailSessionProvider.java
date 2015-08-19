package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.IMAPMailAccountProperties;

import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-01-22.
 */
public interface IIMAPMailSessionProvider
{
    Store connect(String profile) throws Exception;

    IMAPMailAccountProperties getProperties(String profileName);
}
