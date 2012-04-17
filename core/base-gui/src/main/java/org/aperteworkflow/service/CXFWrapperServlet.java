package org.aperteworkflow.service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.ws.Endpoint;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class CXFWrapperServlet extends CXFNonSpringServlet {
    @Override
    public void loadBus(ServletConfig servletConfig) throws ServletException {
        super.loadBus(servletConfig);

        // You could add the endpoint publish codes here
        Bus bus = getBus();
        BusFactory.setDefaultBus(bus);
        Endpoint.publish("/data", new AperteWorkflowDataServiceImpl());
        Endpoint.publish("/process", new AperteWorkflowProcessServiceImpl());

        // You can also use the simple frontend API to do this
//        ServerFactoryBean factory = new ServerFactoryBean();
//        factory.setBus(bus);
//        factory.setServiceClass(AperteWorkflowDataServiceImpl.class);
//        factory.setAddress("/data");
//        factory.create();
    }
}
