package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import java.io.InputStream;
import java.util.List;

public interface JbpmRepository {
	List<byte[]> getAllResources(String type);
	byte[] getResource(String deploymentId, String resourceId);
	String addResource(String resourceId, InputStream definitionStream);
	void addResource(String deploymentId, String resourceId, InputStream definitionStream);
}
