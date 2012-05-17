package pl.net.bluesoft.rnd.processtool.ui.basewidgets.editor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Retention(RUNTIME) @Target({FIELD})
public @interface AvailableOptions {
    String[] value();
}
