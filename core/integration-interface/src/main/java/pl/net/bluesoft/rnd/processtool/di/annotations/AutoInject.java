package pl.net.bluesoft.rnd.processtool.di.annotations;

import java.lang.annotation.*;

import pl.net.bluesoft.rnd.processtool.di.ClassDependencyManager;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;

/**
 * Annotations witch perform auto dependency injection, base on 
 * {@link ClassDependencyManager} and {@link ObjectFactory} classes. 
 * 
 * To auto inject object instanced (only void constructor) use 
 * {@link ClassDependencyManager#
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface AutoInject 
{

}
