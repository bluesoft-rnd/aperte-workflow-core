package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.addons;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;

public interface INotificationsAddonsManager 
{
	/** Create additional variables */
	void addData(TemplateData templateData, BpmTask task, ProcessToolContext ctx);
}
