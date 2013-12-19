package pl.net.bluesoft.rnd.processtool.process.externalkey;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: POlszewski
 * Date: 2012-05-30
 * Time: 11:55
 */
public class PatternUtil {
    public static String substitute(String pattern, Map<String, Object> args) {
        Pattern p = Pattern.compile("\\{([^}:]+):?([^}]*)\\}");
        Matcher m = p.matcher(pattern);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find();

        while(result) {
            String param = m.group(1);
            String format = m.group(2);
            Object arg = args.get(param);

            m.appendReplacement(sb, formatArg(arg, format));
            result = m.find();
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String formatArg(Object arg, String format) {
        if (arg == null) {
            return "????";
        }
        if (arg instanceof Number) {
            return String.format("%" + format + "d", arg);
        }
        if (arg instanceof String) {
            String s = (String)arg;
            if ("l".equals(format) || "L".equals(format)) {
                return s.toLowerCase();
            }
            if ("u".equals(format) || "U".equals(format)) {
                return s.toUpperCase();
            }
            return s;
        }
        return String.valueOf(arg);
    }
}
