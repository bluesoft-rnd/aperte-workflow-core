package org.aperteworkflow.samples.application.ws;

import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.aperteworkflow.samples.application.service.RegisterApplicationService;
import org.aperteworkflow.samples.application.ws.beans.RegisterApplicationServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ISettingsProvider;

import javax.security.auth.callback.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Dominik Dębowczyk on 2015-08-03.
 */
public class Activator implements BundleActivator {

    @Autowired
    private RegisterApplicationServiceImpl registerApplicationService;

    @Autowired
    private ISettingsProvider settingsProvider;

    private final static String APPLICATION_WS = "registerApplication";


    private Set<Server> servers = new HashSet<Server>();

    private final Logger logger = Logger.getLogger(Activator.class.getName());

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        buildEndpoint(APPLICATION_WS, RegisterApplicationService.class, registerApplicationService);
    }

    private <T> void buildEndpoint(final String serviceName, final Class<T> port, final T impl) {
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(port);
        svrFactory.setAddress("/registerApplication");
        svrFactory.setBus(BusFactory.getDefaultBus());
        svrFactory.getProperties(true).put(Message.SCHEMA_VALIDATION_ENABLED, true);
        svrFactory.setServiceBean(impl);
        svrFactory.getInInterceptors().add(new LoggingInInterceptor());
        svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
        svrFactory.getInInterceptors().add(new SAAJInInterceptor());
        servers.add(svrFactory.create());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        for (Server server : servers) {
            try {
                server.stop();
                server.destroy();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(),e);
            }
        }
        servers.clear();

        logger.info("Deactivating the application-ws plugin");
    }

}
