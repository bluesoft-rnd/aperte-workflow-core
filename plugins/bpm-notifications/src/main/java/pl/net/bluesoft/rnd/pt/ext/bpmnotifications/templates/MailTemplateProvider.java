package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.hibernate.Criteria;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateData;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Provider class for the mail templates using for mail services
 * 
 * @author mpawlak
 *
 */
public class MailTemplateProvider implements IMailTemplateLoader
{
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String SENDER_TEMPLATE_SUFFIX = "_sender";
	private static final String SENTFOLDERNAME_TEMPLATE_SUFFIX = "_sentfolderName";
    
    private Logger logger = Logger.getLogger(MailTemplateProvider.class.getName());
    
    private Configuration freemarkerConfiguration;
    
    private Map<String, BpmNotificationTemplate> templateMap = new HashMap<String, BpmNotificationTemplate>();
    
    /** Cache for the */
    private Map<String, String> templateCache = new HashMap<String, String>();
    
    /** Refresh config: look for modifictations of templates in database */
    @SuppressWarnings("unchecked")
	public void refreshConfig() 
    {
        Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
        
        List<BpmNotificationTemplate> templates = 
        		(List<BpmNotificationTemplate>)session
        		.createCriteria(BpmNotificationTemplate.class)
        		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
        		.list();
        
        for (BpmNotificationTemplate t : templates) 
        {
            templateMap.put(t.getTemplateName(), t);
            templateCache.put(t.getTemplateName(), getTemplateBody(t, templates));
            templateCache.put(t.getTemplateName() + SUBJECT_TEMPLATE_SUFFIX, t.getSubjectTemplate() != null
                    ? t.getSubjectTemplate().replaceAll("\\\\u", "\\u") : "");
            templateCache.put(t.getTemplateName() + SENDER_TEMPLATE_SUFFIX, t.getSender());
			templateCache.put(t.getTemplateName() + SENTFOLDERNAME_TEMPLATE_SUFFIX, t.getSentFolderName());
        }
        
        freemarkerConfiguration = new Configuration();
        freemarkerConfiguration.setTemplateLoader(this);
    }

	private String getTemplateBody(BpmNotificationTemplate t, List<BpmNotificationTemplate> templates) {
		String body = t.getTemplateBody() != null ? t.getTemplateBody().replaceAll("\\\\u", "\\u") : "";
		if (hasText(t.getFooterTemplate()) && templates != null) {
			BpmNotificationTemplate footerTemplate = getTemplate(templates, t.getFooterTemplate());
			if (footerTemplate != null) {
				String footer = getTemplateBody(footerTemplate, templates);
				String separator = "<br/>";
				StringBuilder sb = new StringBuilder(body.length() + footer.length() + separator.length());
				body = sb.append(body).append(separator).append(footer).toString();
			}
			else {
				logger.severe("No footer template " + t.getFooterTemplate());
			}
		}
		return body;
	}

	private BpmNotificationTemplate getTemplate(List<BpmNotificationTemplate> templates, String templateName) {
		for (BpmNotificationTemplate template : templates) {
			if (templateName.equals(template.getTemplateName())) {
				return template;
			}
		}
		return null;
	}

	public String findTemplate(String templateName) {
        try {
            return (String) findTemplateSource(templateName);
        }
        catch (IOException e) {
            throw new ProcessToolTemplateErrorException(e);
        }
    }

	@Override
	public String getTemplateSentFolderName(String templateName) {
		return templateCache.get(templateName + SENTFOLDERNAME_TEMPLATE_SUFFIX);
	}

	public BpmNotificationTemplate getBpmNotificationTemplate(String templateName)
	{
		return templateMap.get(templateName);
	}
    
	public String processTemplate(String templateName, TemplateData templateData) throws ProcessToolTemplateErrorException
	{
        logger.info("Using template " + templateName);
        StringWriter sw = new StringWriter();
        try 
        {
            Template template = freemarkerConfiguration.getTemplate(templateName);
            template.process(templateData.getData() != null ? templateData.getData() : new HashMap(), sw);
        }
        catch (Exception e) {
            throw new ProcessToolTemplateErrorException(e);
        }
        sw.flush();
        return sw.toString();
    }
	

	@Override
	public Object findTemplateSource(String templateName) throws IOException
	{
		return templateCache.containsKey(templateName) ? templateCache.get(templateName) : null;
	}

	@Override
	public long getLastModified(Object templateSource)
	{
		return 0;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException
	{
        if (templateSource == null) {
            return null;
        }
        return new StringReader(((String) templateSource));
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException
	{

		
	}

}
