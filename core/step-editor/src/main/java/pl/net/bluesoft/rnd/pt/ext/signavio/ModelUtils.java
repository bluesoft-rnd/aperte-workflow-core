package pl.net.bluesoft.rnd.pt.ext.signavio;

import java.io.File;
import java.util.StringTokenizer;

public class ModelUtils {

    /**
     * Converts the path from Signavio Core format into more normal representation
     *
     * @param parent Parent directory in Signavio Core format e.g. root-directory;subdir;secondsubdir
     * @return Relative filesystem path e.g. subdir/secondsubdir
     */
    public String getParentDirectoryPath(String parent) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(parent, ";");
        while (tokenizer.hasMoreElements()) {
            builder.append(tokenizer.nextElement());
            if (tokenizer.hasMoreElements()) {
                builder.append(File.separator);
            }
        }
        return builder.toString();
    }

}
