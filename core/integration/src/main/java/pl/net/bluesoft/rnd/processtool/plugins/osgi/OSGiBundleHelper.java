package pl.net.bluesoft.rnd.processtool.plugins.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static pl.net.bluesoft.util.lang.StringUtil.hasText;

public class OSGiBundleHelper {
	public static final String		MODEL_ENHANCEMENT	    = "ProcessTool-Model-Enhancement";
	public static final String		WIDGET_ENHANCEMENT	    = "ProcessTool-Widget-Enhancement";
	public static final String		BUTTON_ENHANCEMENT  	= "ProcessTool-Button-Enhancement";
	public static final String		STEP_ENHANCEMENT	    = "ProcessTool-Step-Enhancement";
	public static final String		I18N_PROPERTY		    = "ProcessTool-I18N-Property";
	public static final String		PROCESS_DEPLOYMENT	    = "ProcessTool-Process-Deployment";
	public static final String		GLOBAL_DICTIONARY	    = "ProcessTool-Global-Dictionary";
	public static final String		ICON_RESOURCES		    = "ProcessTool-Resources-Icons";
	public static final String		HUMAN_NAME			    = "Bundle-HumanName-Key";
	public static final String      DESCRIPTION_KEY         = "Bundle-Description-Key";
	public static final String		RESOURCES		        = "ProcessTool-Resources";
	public static final String		ROLE_FILES			    = "ProcessTool-Role-Files";
	public static final String 		IMPLEMENTATION_BUILD    = "Implementation-Build";
    public static final String      TASK_ITEM_ENHANCEMENT   = "ProcessTool-TaskItem-Enhancement";
    public static final String      DESCRIPTION             = Constants.BUNDLE_DESCRIPTION;
    public static final String      HOMEPAGE_URL            = Constants.BUNDLE_UPDATELOCATION;
    public static final String      DOCUMENTATION_URL       = Constants.BUNDLE_DOCURL;

	public static final String[]	HEADER_NAMES		    = {
            MODEL_ENHANCEMENT, WIDGET_ENHANCEMENT, BUTTON_ENHANCEMENT, STEP_ENHANCEMENT, I18N_PROPERTY,
		    PROCESS_DEPLOYMENT, GLOBAL_DICTIONARY, ICON_RESOURCES, RESOURCES, HUMAN_NAME, DESCRIPTION_KEY,
            ROLE_FILES, IMPLEMENTATION_BUILD, TASK_ITEM_ENHANCEMENT, DESCRIPTION, HOMEPAGE_URL, DOCUMENTATION_URL
    };

	private Map<String, String[]> parsedHeadersMap;
	private Bundle bundle;

	public OSGiBundleHelper(Bundle bundle) {
		this.bundle = bundle;
        parsedHeadersMap = new HashMap<String, String[]>();
		processHeaders();
	}

	private void processHeaders() {
		for (String headerName : HEADER_NAMES) {
			String headerValue = (String) bundle.getHeaders().get(headerName);
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
