package pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.entry;

import java.lang.annotation.*;

/**
 * User: POlszewski
 * Date: 2012-01-03
 * Time: 21:49:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface ComplexContent {
	Class elementClass();
	String separator() default ",";
	boolean defaultNull() default false; 
}
