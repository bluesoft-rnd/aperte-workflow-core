package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.*;

/**
 * Annotation used to group separate widgets into a group
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface WidgetGroup {

    /**
     * The name used to aggregate widgets into a widget group
     * @return Widget group name
     */
     String value();

}
