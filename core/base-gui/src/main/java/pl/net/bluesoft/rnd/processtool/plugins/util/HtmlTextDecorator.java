package pl.net.bluesoft.rnd.processtool.plugins.util;

import java.util.ArrayList;
import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Text decorator for servlet using html 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class HtmlTextDecorator  implements IWriterTextDecorator
{

	private I18NSource i18NSource;
	private Collection<String> messages;
	
	public HtmlTextDecorator(I18NSource i18NSource)
	{
		this.i18NSource = i18NSource;
		this.messages = new ArrayList<String>();
	}

	@Override
	public void addText(String text) 
	{
		messages.add(text);
	}

	@Override
	public String getOutput() {
		StringBuilder builder = new StringBuilder();
		
		builder.append( "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n");
		builder.append("<HTML>\n<HEAD>");
		
		builder.append("<TITLE>" + i18NSource.getMessage("token.servlet.title") + "</TITLE>");
		builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"../"+ProcessToolBpmConstants.APERTEWORKFLOW_CSS_PATH+"\">");
		
		builder.append("</HEAD>\n");
		builder.append("<BODY>\n");
		builder.append("<div class=\"servlet-body\">\n");
		builder.append("<div class=\"servlet-response-top\" role=\"contentinfo\"><a href=\"http://bluesoft.net.pl\" rel=\"external\" id=\"bluesoftlink\" title=\"Bluesoft\">&nbsp;" +
				"</a> <a href=\"http://aperteworkflow.org\" rel=\"external\" id=\"apertelink\" title=\"Aperte Workflow\">&nbsp;</a> </p> </div>");
		
		builder.append("<div class=\"servlet-response-messages\">");
		
		for(String message: messages)
		{
			builder.append("<div class=\"servlet-response-line-message\">");
			builder.append(message);
			builder.append("</div>");
		}
		
		builder.append("</div></div>");

		builder.append("</BODY></HTML>");
		
		return builder.toString();
	}
}
