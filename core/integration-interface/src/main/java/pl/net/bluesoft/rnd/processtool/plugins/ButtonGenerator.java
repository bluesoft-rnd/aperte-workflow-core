package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * User: POlszewski
 * Date: 2014-08-13
 */
public interface ButtonGenerator {
	interface Callback {
		void createButton(int priority, String actionButtonId, String buttonClass, String iconClass, String messageKey,
						  String descriptionKey, String clickFunction);
		void appendScript(String script);
	}

	void generate(IAttributesProvider viewedObject, boolean objectIsClosed, boolean userCanPerformActions, Callback callback);
}
