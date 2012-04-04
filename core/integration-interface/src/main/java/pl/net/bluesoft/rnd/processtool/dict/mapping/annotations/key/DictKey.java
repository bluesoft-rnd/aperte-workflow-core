package pl.net.bluesoft.rnd.processtool.dict.mapping.annotations.key;

import java.lang.annotation.*;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 15:14:45
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface DictKey {
    String dict();    
}
