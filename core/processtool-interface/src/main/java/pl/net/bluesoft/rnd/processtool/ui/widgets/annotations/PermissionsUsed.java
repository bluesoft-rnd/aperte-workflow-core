package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface PermissionsUsed {

    Permission[] value() default {
        @Permission(key="EDIT",desc="permission.desc.EDIT"),
        @Permission(key="VIEW",desc="permission.desc.VIEW")
    };
}

