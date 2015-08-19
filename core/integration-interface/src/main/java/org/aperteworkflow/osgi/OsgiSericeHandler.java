package org.aperteworkflow.osgi;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 15.05.14
 * Time: 13:26
 */
public interface OsgiSericeHandler {
	public String getDefinition();
	public String handle(String method, String content);
	public String getPath();
}
