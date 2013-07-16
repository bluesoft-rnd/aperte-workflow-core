package pl.net.bluesoft.rnd.pt.ext.jbpm.service;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultJbpmRepository implements JbpmRepository {
	private static final String DEFAULT_BASE_PATH = ".." + File.separator + ".." + File.separator + "jbpm" + File.separator + "repository";

	private final String basePath;

	public DefaultJbpmRepository(String basePath) {
		this.basePath = basePath != null ? basePath : DEFAULT_BASE_PATH;
		ensureBasePath();
	}

	@Override
	public List<byte[]> getAllResources(String type) {
		try {
			File base = new File(basePath);
			Collection<File> files = FileUtils.listFiles(base, new String[] { type }, true);
			List<byte[]> result = new ArrayList<byte[]>(files.size());

			for (File file : files) {
				result.add(FileUtils.readFileToByteArray(file));
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] getResource(String deploymentId, String resourceId) {
		try {
			File file = new File(getPath(deploymentId,resourceId));
			return FileUtils.readFileToByteArray(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String addResource(String resourceId, InputStream definitionStream) {
		String deploymentId = getDeploymentId();
		addResource(deploymentId, resourceId, definitionStream);
		return deploymentId;
	}

	@Override
	public void addResource(String deploymentId, String resourceId, InputStream definitionStream) {
		try {
			File file = new File(getPath(deploymentId,resourceId));
			FileUtils.copyInputStreamToFile(definitionStream, file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getDeploymentId() {
		return String.valueOf(System.currentTimeMillis());
	}
	
	private String getPath(String deploymentId, String resourceId) {
		return basePath + File.separator + deploymentId + File.separator + resourceId;
	}

	private void ensureBasePath() {
		if (basePath != null) {
			new File(basePath).mkdirs();
		}
	}
}
