package org.aperteworkflow.samples.application.ws;

import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.aperteworkflow.samples.application.service.RegisterApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 24.07.14
 * Time: 19:42
 */
@Component
public class WsClient {

	@Autowired
	private WsConfig config;

	public RegisterApplicationService getService() throws Exception {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(RegisterApplicationService.class);
		factory.setAddress(config.getServiceEndpointUrl());
		factory.getOutInterceptors().add(new SAAJOutInterceptor());

		RegisterApplicationService service = (RegisterApplicationService) factory.create();
		return service;
	}

}
