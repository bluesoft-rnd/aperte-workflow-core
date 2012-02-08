package pl.net.bluesoft.rnd.processtool.ui.widgets.annotations;

import com.vaadin.ui.Field;

import java.lang.annotation.*;

/**
 * Annotation used to influence step-editor behaviour when displaying properties
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface AutoWiredPropertyConfigurator {

    //
    // no value() used, probably other options are going to be introduced soon
    //

    /**
     * Class used to render specific property field
     * @return Specific class
     */
    Class<? extends Field> fieldClass();

}
