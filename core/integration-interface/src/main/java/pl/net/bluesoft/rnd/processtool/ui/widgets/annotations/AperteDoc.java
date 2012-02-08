package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
@Inherited
public @interface AperteDoc {
    final static String	DEFAULT_ICON	= "";

    String humanNameKey();

    String descriptionKey();

    String icon() default DEFAULT_ICON;

    boolean internal() default false;

}
