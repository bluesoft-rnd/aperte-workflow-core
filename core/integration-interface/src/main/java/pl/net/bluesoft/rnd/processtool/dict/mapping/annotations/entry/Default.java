package pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.entry;

import java.lang.annotation.*;

/**
 * User: POlszewski
 * Date: 2012-01-03
 * Time: 21:52:37
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Default {
	String value();
}
