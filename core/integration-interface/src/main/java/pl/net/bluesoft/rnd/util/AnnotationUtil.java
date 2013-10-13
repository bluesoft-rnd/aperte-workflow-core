package pl.net.bluesoft.rnd.util;

import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;

/**
 * User: POlszewski
 * Date: 2013-10-09
 * Time: 14:48
 */
public class AnnotationUtil {
	public static String getAliasName(Class<?> clazz) {
		AliasName aliasName = clazz.getAnnotation(AliasName.class);
		return aliasName != null ? aliasName.name() : clazz.getSimpleName();
	}
}
