package pl.net.bluesoft.rnd.processtool.web.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface OsgiController
{
    String name();
}
