package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PermissionsUsed {

    Permission[] value() default {
        @Permission(key="EDIT",desc="widget.permission.desc.EDIT"),
        @Permission(key="VIEW",desc="widget.permission.desc.VIEW")
    };
}

