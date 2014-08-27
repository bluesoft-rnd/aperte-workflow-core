package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
public interface IBundleResourceProvider
{
    InputStream getBundleResourceStream(String resourcePath) throws IOException;
	String getBundleResourceString(String resourcePath) throws IOException;
}
