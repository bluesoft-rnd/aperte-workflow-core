package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aperteworkflow.util.liferay.LiferayBridge;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import pl.net.bluesoft.rnd.processtool.plugins.util.DictionaryHelpChanger;
import pl.net.bluesoft.rnd.processtool.plugins.util.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.plugins.util.UserProcessQueuesSizeProvider.UsersQueuesSize;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.servlet.PortalDelegateServlet;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.thoughtworks.xstream.XStream;

/**
 * Servlet with dictionary update logic. It requires active liferay session 
 * and CHANGE_HELP_TOOLTIPS role to proceed
 * 
 * @author mpawlak@bluesoft.net.pl
 * 
 */
public class HelpContextChangerServlet extends AbstractLiferayServlet 
{
	private static Set<String> authorizedRoles = new HashSet<String>();
	
	static 
	{
		authorizedRoles.add("CHANGE_HELP_TOOLTIPS");
	}

	private static Logger logger = Logger.getLogger(HelpContextChangerServlet.class.getName());
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final XStream xstream = new XStream();
	private static final Format DEFAULT_FORMAT = Format.JSON;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		if(isAuthorizationRequired())
		{
			boolean isUserAuthorized = authorizeUserByRequest(req, resp);
			
			if(!isUserAuthorized)
				return;
		}
		
		processRequest(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		if(isAuthorizationRequired())
		{
			boolean isUserAuthorized = authorizeUserByRequest(req, resp);
			
			if(!isUserAuthorized)
				return;
		}
		
		processRequest(req, resp);
	}
	
	private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
	{
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();

		try
		{
			String processDefinitionName = getRequestParamter(req, "processDefinitionName");
			String dictionaryId = getRequestParamter(req, "dictionaryId");
			String languageCode = getRequestParamter(req, "languageCode");
			String dictionaryItemKey = getRequestParamter(req,"dictionaryItemKey");
			String dictionaryItemValue = getRequestParamter(req,"dictionaryItemValue");
			
			DictionaryHelpChanger.DictionaryChangeRequest dictionaryChangeRequest = new DictionaryHelpChanger.DictionaryChangeRequest()
				.setProcessDeifinitionName(processDefinitionName)
				.setDictionaryId(dictionaryId)
				.setLanguageCode(languageCode)
				.setDictionaryItemKey(dictionaryItemKey)
				.setDictionaryItemValue(dictionaryItemValue);
			
			/* All parameters specified, proceed with dictionary item change */
			ProcessToolRegistry registry = (ProcessToolRegistry) getServletContext().getAttribute(ProcessToolRegistry.class.getName());
			
			DictionaryHelpChanger helpChanger = new DictionaryHelpChanger(registry);
			helpChanger.changeDictionaryHelp(dictionaryChangeRequest);
		}
		catch(Exception ex)
		{
			out.write("Problem during processing request");
			out.write(ex.getMessage());
			
			logger.log(Level.WARNING, "Problem during processing request", ex);
		}
		
		out.close();
	}

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info(this.getClass().getSimpleName() + " INITIALIZED: "
				+ getServletContext().getContextPath());
	}

	@Override
	public void destroy() {
		super.destroy();
		logger.info(this.getClass().getSimpleName() + " DESTROYED");
	}

	@Override
	public Set<String> getAuthorizedRoles() 
	{
		return authorizedRoles;
	}

	@Override
	public String getSessionAuthorizationName() 
	{
		return HelpContextChangerServlet.class.getName()+"_Authorization";
	}

	@Override
	public boolean isAuthorizationRequired() 
	{
		return true;
	}
}
