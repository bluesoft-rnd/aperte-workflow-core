package org.aperteworkflow.editor.signavio;

import org.aperteworkflow.editor.domain.ProcessModelConfig;

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

    /**
     * Get the model file full prefix
     *
     * @param config
     * @return
     */
    public static String getModelFilePrefix(ProcessModelConfig config) {
        String prefix = config.getModelerRepoDirectory() + File.separator;
        prefix += getParentDirectoryPath(config.getDirectory()) + File.separator;
        prefix += config.getFileName();
        return prefix;
    }

    /**
     * Get the path where one should store the process logo for this model
     *
     * @param config
     * @return
     */
    public static String getModelLogoFilePath(ProcessModelConfig config) {
        String prefix = ModelUtils.getModelFilePrefix(config);
        return (prefix + "." + ModelConstants.PROCESS_LOGO_FILE_NAME);
    }

}
