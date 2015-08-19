package org.aperteworkflow.osgi;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 15.05.14
 * Time: 13:22
 */
public interface OsgiServiceDispatcher {
	String handle(HttpServletRequest req) throws IOException;
	void install(OsgiSericeHandler handler);
	void uninstall(OsgiSericeHandler handler);
}
