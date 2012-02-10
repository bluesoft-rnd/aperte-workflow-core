package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Permission {
    String key();
    String desc() default "";
}
