package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Provider class for the mail templates using for mail services
 * 
 * @author mpawlak
 *
 */
public class MailTemplateProvider implements TemplateLoader
{
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String SENDER_TEMPLATE_SUFFIX = "_sender";
    
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
            templateCache.put(t.getTemplateName(), t.getTemplateBody() != null ? t.getTemplateBody().replaceAll("\\\\u", "\\u") : "");
            templateCache.put(t.getTemplateName() + SUBJECT_TEMPLATE_SUFFIX, t.getSubjectTemplate() != null
                    ? t.getSubjectTemplate().replaceAll("\\\\u", "\\u") : "");
            templateCache.put(t.getTemplateName() + SENDER_TEMPLATE_SUFFIX, t.getSender());
        }
        
        freemarkerConfiguration = new Configuration();
        freemarkerConfiguration.setTemplateLoader(this);
    }
    
    public String findTemplate(String templateName) {
        try {
            return (String) findTemplateSource(templateName);
        }
        catch (IOException e) {
            throw new ProcessToolTemplateErrorException(e);
        }
    }
    
	public BpmNotificationTemplate getBpmNotificationTemplate(String templateName)
	{
		return templateMap.get(templateName);
	}
    
	public String processTemplate(String templateName, Map data) 
	{
        logger.info("Using template " + templateName);
        StringWriter sw = new StringWriter();
        try 
        {
            Template template = freemarkerConfiguration.getTemplate(templateName);
            template.process(data != null ? data : new HashMap(), sw);
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
