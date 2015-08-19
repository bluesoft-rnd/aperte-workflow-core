package org.aperteworkflow.service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.servlet.ServletConfig;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 04.07.14
 * Time: 09:44
 */
public class CXFOsgiWrapperServler  extends CXFNonSpringServlet {
	@Override
	public void loadBus(ServletConfig servletConfig) {
		super.loadBus(servletConfig);
		// You could add the endpoint publish codes here
		Bus bus = getBus();
		//Non-Core WS should be published via this sevlet
		BusFactory.setDefaultBus(bus);
	}
}
