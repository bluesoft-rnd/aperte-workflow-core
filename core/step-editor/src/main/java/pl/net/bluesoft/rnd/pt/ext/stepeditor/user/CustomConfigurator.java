package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface CustomConfigurator {

	Class<? extends WidgetConfigFormFieldFactory> value();

}
