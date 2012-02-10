package pl.net.bluesoft.rnd.util.i18n.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface PropertyLoader {

	InputStream loadProperty(String path) throws IOException;
}
