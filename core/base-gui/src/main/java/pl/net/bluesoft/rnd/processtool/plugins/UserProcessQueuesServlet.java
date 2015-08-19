package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider.UserQueueDTO;
import pl.net.bluesoft.rnd.processtool.userqueues.UserProcessQueuesSizeProvider.UsersQueuesDTO;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import com.thoughtworks.xstream.XStream;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Servlet which provides logic to get all avaiable user process queues
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueuesServlet extends HttpServlet 
{
	public enum Format {
		JSON, XML
	}

	private static Logger				logger			= Logger.getLogger(UserProcessQueuesServlet.class.getName());
	private static final ObjectMapper	mapper			= new ObjectMapper();
	private static final XStream		xstream			= new XStream();
	private static final Format			DEFAULT_FORMAT	= Format.JSON;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		resp.setContentType("application/json");
		
		PrintWriter out = resp.getWriter();
		String formatString = req.getParameter("format");
		Format format;
		if (formatString == null) {
			format = DEFAULT_FORMAT;
		} else {
			format = Format.valueOf(formatString.toUpperCase());
		}
		
		String userLogin = req.getParameter("userLogin");
		if(userLogin == null)
		{
			out.write("No user login specified. Please run servlet with 'userLogin' parameter");
			return;
		}
		
		I18NSource messageSource = I18NSourceFactory.createI18NSource(req.getLocale());
		
		UserProcessQueuesSizeProvider userQueuesSizeProvider = new UserProcessQueuesSizeProvider(userLogin, messageSource);
		Collection<UsersQueuesDTO> usersQueuesSize = userQueuesSizeProvider.getUserProcessQueueSize();
		
		Map<String, Map<String, Integer>> usersQueues = new HashMap<String, Map<String,Integer>>(usersQueuesSize.size());
		for(UsersQueuesDTO userQuueueSize: usersQueuesSize)
		{
			Map<String, Integer> userQueueMap = new HashMap<String, Integer>();
			
			for(UserQueueDTO userQueue: userQuueueSize.getQueuesList())
				userQueueMap.put(userQueue.getQueueId(), userQueue.getQueueSize());

			usersQueues.put(userQuueueSize.getUserLogin(), userQueueMap);
		}


		switch (format) {
			case XML: {
				out.write(xstream.toXML(usersQueues));
				break;
			}
			case JSON: {
				mapper.configure(Feature.INDENT_OUTPUT, true);
				mapper.writeValue(out, usersQueues);
				break;
			}
		}

		out.close();

		logger.info(this.getClass().getSimpleName() + " GET");
	}

	@Override
	public void init() throws ServletException {
		super.init();
		logger.info(this.getClass().getSimpleName() + " INITIALIZED: " + getServletContext().getContextPath());
	}

	@Override
	public void destroy() {
		super.destroy();
		logger.info(this.getClass().getSimpleName() + " DESTROYED");
	}
}
