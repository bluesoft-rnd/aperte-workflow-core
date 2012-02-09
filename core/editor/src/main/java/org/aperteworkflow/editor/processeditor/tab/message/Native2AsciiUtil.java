package org.aperteworkflow.editor.processeditor.tab.message;

public class Native2AsciiUtil {

    /**
     * Encode the native format to the ASCII string with
     *
     * @param input
     * @return
     */
    public static String native2Ascii(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(input.length() + 60);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c <= 126) {
                builder.append(c);
            } else {
                builder.append("\\u");
                String hex = Integer.toHexString(c);
                for (int j = hex.length(); j < 4; j++) {
                    builder.append('0');
                }
                builder.append(hex);
            }
        }
        return builder.toString();
    }

    /**
     * Decode the ASCII string to the native format
     *
     * @param input  ASCII input
     * @return String in native format
     */
    public static String ascii2Native(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(input.length());
        boolean precedingBackslash = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (precedingBackslash) {
                switch (c) {
                    case 'f': c = '\f'; break;
                    case 'n': c = '\n'; break;
                    case 'r': c = '\r'; break;
                    case 't': c = '\t'; break;
                    case 'u':
                        String hex = input.substring(i + 1, i + 5);
                        c = (char) Integer.parseInt(hex, 16);
                        i += 4;
                        break;
                }
                precedingBackslash = false;
            } else {
                precedingBackslash = (c == '\\');
            }
            if (!precedingBackslash) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}
