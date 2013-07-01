package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import java.io.InputStream;

public interface JbpmRepository {
	byte[][] getAllResources(String type);
	byte[] getResource(String deploymentId, String resourceId);
	String addResource(String resourceId, InputStream definitionStream);
	void addResource(String deploymentId, String resourceId, InputStream definitionStream);
}
