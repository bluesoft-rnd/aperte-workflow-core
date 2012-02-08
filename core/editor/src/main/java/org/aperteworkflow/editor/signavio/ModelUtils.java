package org.aperteworkflow.editor.signavio;

import java.io.File;
import java.util.StringTokenizer;

public class ModelUtils {

    /**
     * Converts the path from Signavio Core format into more normal representation
     *
     * @param parent Parent directory in Signavio Core format e.g. root-directory;subdir;secondsubdir
     * @return Relative filesystem path e.g. subdir/secondsubdir
     */
    public static String getParentDirectoryPath(String parent) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(parent, ";");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.matches(ModelConstants.MODEL_ROOT_DIRECTORY_PATTERN)) {
                // skip the root indicator, it is useless here
                continue;
            }
            builder.append(token);
            if (tokenizer.hasMoreTokens()) {
                builder.append(File.separator);
            }
        }
        return builder.toString();
    }

}
