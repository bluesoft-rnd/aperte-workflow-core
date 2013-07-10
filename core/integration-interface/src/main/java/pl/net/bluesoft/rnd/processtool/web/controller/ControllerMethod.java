package pl.net.bluesoft.rnd.processtool.web.controller;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * Annotation for controller method
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface ControllerMethod
{
    /** Controller action name */
    String action();
}
