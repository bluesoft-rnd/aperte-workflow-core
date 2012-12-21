package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.BpmNotificationEngine;
import pl.net.bluesoft.util.eventbus.EventListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Obsluga eventa wysylania maila
 * @author marcin
 *
 */
public class MailEventListener implements EventListener<MailEvent> {
	
	private BpmNotificationEngine engine;
	private final static Logger logger = Logger.getLogger(MailEventListener.class.getName());
	
	public MailEventListener(BpmNotificationEngine engine) {
		this.engine = engine;
	}

	@Override
	public void onEvent(MailEvent e) {
		
		logger.info("Handling mail event");
		
		try 
		{	
			engine.addNotificationToSend(e.getMailSessionProfileName(), e.getSender(), e.getRecipient(), e.getSubject(), e.getBody(), false, e.getAttachments());
		}
		catch (Exception ex) {
			logger.log(Level.SEVERE, "Error sending email with attachments", ex);
		}
				
	}

}
