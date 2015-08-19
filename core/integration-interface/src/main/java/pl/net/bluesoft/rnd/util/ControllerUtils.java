package pl.net.bluesoft.rnd.util;

import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;

/**
 * Created by mpawlak@bluesoft.net.pl on 2014-12-11.
 */
public class ControllerUtils {

    public static Boolean getBooleanParameter(OsgiWebRequest invocation, String name) {
        String value = invocation.getRequest().getParameter(name);
        if(StringUtils.isEmpty(value))
        {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public static Integer getIntegerParameter(OsgiWebRequest invocation, String name) {
        String value = invocation.getRequest().getParameter(name);
        if(StringUtils.isEmpty(value))
        {
            return null;
        }
        return Integer.parseInt(value);
    }

    public static String getParameter(OsgiWebRequest invocation, String name) {
        return invocation.getRequest().getParameter(name);
    }
}
