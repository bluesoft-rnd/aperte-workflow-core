package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

public class OSGiBundleHelper {
	private Map<String, String[]> parsedHeadersMap;
	private Bundle bundle;

	public OSGiBundleHelper(Bundle bundle) {
		this.bundle = bundle;
        parsedHeadersMap = new HashMap<String, String[]>();
		processHeaders();
	}

	private void processHeaders() {
		for (String headerName : BundleInstallationHandler.HEADER_NAMES) {
			String headerValue = bundle.getHeaders().get(headerName);
			if (hasText(headerValue)) {
				parsedHeadersMap.put(headerName, headerValue.replaceAll("\\s*", "").split(","));
			}
		}
	}

	public boolean hasHeaderValues(String headerName) {
		return parsedHeadersMap.containsKey(headerName);
	}

	public String[] getHeaderValues(String headerName) {
		return parsedHeadersMap.get(headerName);
	}

	public Bundle getBundle() {
		return bundle;
	}

	public BundleMetadata getBundleMetadata() {
		return new BundleMetadata(bundle.getLocation(), bundle.getSymbolicName(), bundle.getLastModified(), bundle.getState());
	}

	public InputStream getBundleResourceStream(String resourcePath) throws IOException {
		return getBundleResourceStream(bundle, resourcePath);
	}

	public static InputStream getBundleResourceStream(Bundle bundle, String resourcePath) throws IOException {
		URL resource = bundle.getResource(resourcePath);
		return resource != null ? resource.openStream() : null;
	}
}
