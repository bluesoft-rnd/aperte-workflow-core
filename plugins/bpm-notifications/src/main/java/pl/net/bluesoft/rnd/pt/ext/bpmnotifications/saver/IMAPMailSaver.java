package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.saver;

import javax.mail.*;
import java.util.Properties;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-01-22.
 */
public class IMAPMailSaver implements IMailSentSaver
{

    @Override
    public void saveMail(Session mailSession, String folderName, Message message) throws MessagingException {
        Store store = mailSession.getStore("imaps");

        Properties emailPrtoperties = mailSession.getProperties();

        String secureHost = emailPrtoperties.getProperty("mail.smtp.host");
        String securePort = emailPrtoperties.getProperty("mail.smtp.port");
        String userName = emailPrtoperties.getProperty("mail.smtp.user");
        String userPassword = emailPrtoperties.getProperty("mail.smtp.password");

        store.connect(secureHost, userName,userPassword);

        Folder dfolder = getFolder(store, folderName);
        Message[] messages = new Message[1];
        messages[0]= message;
        dfolder.appendMessages(messages);
    }

    private Folder getFolder(Store store, String folderName) throws MessagingException {
        Folder folder = store.getDefaultFolder().getFolder(folderName);
        if (!folder.exists()) {
            folder.create(Folder.HOLDS_MESSAGES);
        }
        return folder;
    }
}
