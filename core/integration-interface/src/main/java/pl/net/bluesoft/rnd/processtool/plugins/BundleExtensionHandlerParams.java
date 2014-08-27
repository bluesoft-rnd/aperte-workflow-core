package pl.net.bluesoft.rnd.processtool.plugins;

/**
 * User: POlszewski
 * Date: 2014-06-26
 */
public interface BundleExtensionHandlerParams {
	int getEventType();
	boolean hasBundleHeader(String headerName);
	String getBundleHeaderValue(String headerName);
	String[] getBundleHeaderValues(String headerName);
	IBundleResourceProvider getBundleResourceProvider();

	Class loadClass(String className);
}
