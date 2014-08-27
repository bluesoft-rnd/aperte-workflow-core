package pl.net.bluesoft.interactivereports.templates;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.interactivereports.service.InteractiveReportServiceImpl;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.template.ProcessToolTemplateErrorException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-06-25
 */
public class HtmlInteractiveReportTemplateLoader implements TemplateLoader {
	private static final String PARAMS_TEMPLATE = "params";
	private static final String BODY_TEMPLATE = "body";

	private final Configuration configuration;
	private final Map<String, String> templateCache = new HashMap<String, String>();
	private static String definitions;

	// file path without extension

	public HtmlInteractiveReportTemplateLoader(IBundleResourceProvider bundleResourceProvider, String templatePath) {
		this.configuration = new Configuration();
		this.configuration.setTemplateLoader(this);

		try {
			String paramsTemplate = bundleResourceProvider.getBundleResourceString(templatePath + "_params.html");
			templateCache.put(PARAMS_TEMPLATE, definitions + paramsTemplate);
		}
		catch (Exception e) {
			throw new RuntimeException("Problem during loading report file: " + templatePath + "_params.html", e);
		}

		try {
			String bodyTemplate = bundleResourceProvider.getBundleResourceString(templatePath + ".html");
			templateCache.put(BODY_TEMPLATE, definitions + bodyTemplate);
		}
		catch (Exception e) {
			throw new RuntimeException("Problem during loading report file: " + templatePath + ".html", e);
		}
	}

	public static void setDefinitions(String definitions) {
		HtmlInteractiveReportTemplateLoader.definitions = definitions;
	}

	public String processParamsTemplate(Map<String, Object> templateData) {
		return processTemplate(PARAMS_TEMPLATE, templateData);
	}

	public String processBodyTemplate(Map<String, Object> templateData) {
		return processTemplate(BODY_TEMPLATE, templateData);
	}

	private String processTemplate(String templateName, Map<String, Object> templateData) throws ProcessToolTemplateErrorException {
		StringWriter sw = new StringWriter();

		try {
			Template template = configuration.getTemplate(templateName);
			template.process(templateData, sw);
		}
		catch (Exception e) {
			throw new ProcessToolTemplateErrorException(e);
		}
		return sw.toString();
	}

	@Override
	public void closeTemplateSource(Object templateName) throws IOException {
	}

	@Override
	public Object findTemplateSource(String templateName) throws IOException {
		return templateCache.containsKey(templateName) ? templateCache.get(templateName) : null;
	}

	@Override
	public long getLastModified(Object arg0) {
		return 0;
	}

	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		if (templateSource == null) {
			return null;
		}
		return new StringReader((String)templateSource);
	}
}
