package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.saver;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-01-22.
 */
public interface IMailSentSaver {
    void saveMail(javax.mail.Session mailSession, String folderName, Message message) throws MessagingException;
}
