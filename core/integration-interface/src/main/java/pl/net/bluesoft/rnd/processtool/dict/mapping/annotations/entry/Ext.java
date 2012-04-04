package pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.entry;

import java.lang.annotation.*;

/**
 * User: POlszewski
 * Date: 2011-12-09
 * Time: 10:11:41
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface Ext {
	String name();		
}
