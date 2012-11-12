package pl.net.bluesoft.rnd.processtool.plugins;

import com.thoughtworks.xstream.XStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.util.lang.Classes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Logger;

public class StepExposureServlet extends HttpServlet {
	public enum Format {
		JSON, XML
	}

	private static final String			NAME			= "name";
	private static final String			TYPE			= "type";
	private static final String			PARAMETERS		= "parameters";
	private static final String			REQUIRED		= "required";
	private static Logger				logger			= Logger.getLogger(StepExposureServlet.class.getName());
	private static final ObjectMapper	mapper			= new ObjectMapper();
	private static final XStream		xstream			= new XStream();
	private static final Format			DEFAULT_FORMAT	= Format.JSON;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ProcessToolRegistry reg = (ProcessToolRegistry) getServletContext().getAttribute(ProcessToolRegistry.class.getName());
		resp.setContentType("text/plain");

		List<Map<String, Object>> steps = new LinkedList<Map<String, Object>>();

        Map<String,ProcessToolProcessStep> availableSteps = reg.getAvailableSteps();
        Set<Class> classes = new HashSet<Class>();
                
        for (ProcessToolProcessStep stepInstance : availableSteps.values()) {
            Class stepClass = stepInstance.getClass();
            classes.add(stepClass);
        }

        for (Class stepClass : classes) {
            Map<String, Object> map = new HashMap<String, Object>();
            AliasName a = Classes.getClassAnnotation(stepClass, AliasName.class);
            map.put(NAME, a.name());
            List<Field> fields = Classes.getFieldsWithAnnotation(stepClass, AutoWiredProperty.class);
            List<Map<String, Object>> parameters = new ArrayList<Map<String, Object>>();
            if (fields != null) {
                for (Field field : fields) {
                    Map<String, Object> parameter = new HashMap<String, Object>();
                    parameter.put(NAME, field.getName());
                    parameter.put(TYPE, field.getType());
                    AutoWiredProperty awp = field.getAnnotation(AutoWiredProperty.class);
                    parameter.put(REQUIRED, awp != null && awp.required());
                    parameters.add(parameter);
                }
            }
            map.put(PARAMETERS, parameters);
            steps.add(map);
        }

		PrintWriter out = resp.getWriter();
		String formatString = req.getParameter("format");
		Format format;
		if (formatString == null) {
			format = DEFAULT_FORMAT;
		} else {
			format = Format.valueOf(formatString.toUpperCase());
		}

		switch (format) {
			case XML: {
				out.write(xstream.toXML(steps));
				break;
			}
			case JSON: {
				mapper.configure(Feature.INDENT_OUTPUT, true);
				mapper.writeValue(out, steps);
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
