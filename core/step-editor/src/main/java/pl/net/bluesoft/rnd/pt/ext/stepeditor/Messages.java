package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = StepEditorApplication.class.getSimpleName();

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key){
		return getString(key, new Object[]{});
	}
	
	public static String getString(String key, Object... parameters) {
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key), parameters);
		} catch (MissingResourceException e) {
			System.err.println("NOT DEFINED KEY: " + key);
			return '!' + key + '!';
		}
	}
}
