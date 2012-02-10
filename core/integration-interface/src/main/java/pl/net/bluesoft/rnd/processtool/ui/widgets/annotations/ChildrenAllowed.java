package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ChildrenAllowed {
    public static boolean DEFAULT = false;
    boolean value() default DEFAULT;
}
