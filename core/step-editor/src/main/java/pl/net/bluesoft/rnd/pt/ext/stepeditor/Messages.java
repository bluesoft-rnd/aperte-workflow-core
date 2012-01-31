package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Messages {
    private static final Logger LOGGER = Logger.getLogger(Messages.class.getName());
	private static final String BUNDLE_NAME = StepEditorApplication.class.getSimpleName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key){
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            LOGGER.warning("NOT DEFINED KEY: " + key);
            return '!' + key + '!';
        }
	}
	
	public static String getString(String key, Object... parameters) {
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key), parameters);
		} catch (MissingResourceException e) {
            LOGGER.warning("NOT DEFINED KEY: " + key);
			return '!' + key + '!';
		}
	}
}
