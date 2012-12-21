package pl.net.bluesoft.rnd.processtool.model;

import pl.net.bluesoft.util.lang.Strings;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public enum ProcessStatus {
    NEW, RUNNING, CANCELLED, FINISHED, UNKNOWN;

    public static ProcessStatus fromString(String name) {
        return Strings.hasText(name) ? valueOf(name.toUpperCase()) : null;
    }

    public static ProcessStatus fromChar(char c) {
        String prefix = ("" + c).toUpperCase();
        ProcessStatus value = null;
        if (Strings.hasText(prefix)) {
            for (ProcessStatus ps : values()) {
                if (ps.name().startsWith(prefix)) {
                    value = ps;
                    break;
                }
            }
        }
        return value;
    }
}
