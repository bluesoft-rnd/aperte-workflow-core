package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AliasName {
	String name();
    /** Type, Vaadin or HTML */
    WidgetType type() default WidgetType.Html;
}
