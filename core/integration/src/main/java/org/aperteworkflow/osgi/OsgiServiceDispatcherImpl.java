package org.aperteworkflow.osgi;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 15.05.14
 * Time: 13:23
 */
public class OsgiServiceDispatcherImpl implements OsgiServiceDispatcher {

	private static final String WSDL = "wsdl";
	private final Object lock = new Object();

	private Map<String, OsgiSericeHandler> handlers = new HashMap<String, OsgiSericeHandler>();
	private String SOAPAction = "SOAPAction";

	@Override
	public String handle(HttpServletRequest req) throws IOException {
		String path = getPath(req);
		OsgiSericeHandler handler = getHandler(path);
		if(definitionRequest(req)){
			return handler.getDefinition();
		}else{
			String action = getAction(req);
			return handler.handle(action, getContentAsString(req));
		}

	}

	private boolean definitionRequest(HttpServletRequest req) {
		String query = req.getQueryString();
		return query != null && query.toLowerCase().contains(WSDL);
	}

	private String getAction(HttpServletRequest req) {
		Enumeration<String> action = req.getHeaders(SOAPAction);
		if(action.hasMoreElements()){
			return action.nextElement();
		}
		return null;
	}

	private String getPath(HttpServletRequest req) {
		String uri = req.getRequestURI();
		uri = uri.replace(req.getContextPath(), "");
		uri= uri.replace(req.getServletPath(), "");
		if(uri.startsWith("/")){
			uri = uri.substring(1);
		}
		return uri;
	}


	private OsgiSericeHandler getHandler(String path) {
		synchronized (lock) {
			return handlers.get(path);
		}
	}




	private String getContentAsString(HttpServletRequest req) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(req.getInputStream(), writer, "UTF-8");
		return writer.toString();
	}



	@Override
	public void install(OsgiSericeHandler handler) {
		final String path = handler.getPath();
		synchronized (lock){
			if(handlers.containsKey(path)){
				throw new RuntimeException("Path is already taken!");
			}
			handlers.put(path, handler);
		}

	}

	@Override
	public void uninstall(OsgiSericeHandler handler) {
		final String path = handler.getPath();
		synchronized (lock){
			if(handlers.containsKey(path) ){
				if(handler == handlers.get(path)){
					handlers.remove(path);
				}
			}
		}
	}


}
