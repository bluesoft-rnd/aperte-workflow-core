package org.aperteworkflow.samples.application.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class WsConfig {
	@Value(value = "#{wsSettings['service.endpoint.url']}")
	protected String serviceEndpointUrl;

	public String getServiceEndpointUrl() {
		return serviceEndpointUrl;
	}

}
