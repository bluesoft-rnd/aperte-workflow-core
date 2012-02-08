package pl.net.bluesoft.rnd.processtool.steps.annotations;

import java.lang.annotation.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface StepName {
	String name();
}
