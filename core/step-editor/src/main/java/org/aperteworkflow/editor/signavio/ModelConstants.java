package org.aperteworkflow.editor.signavio;

public class ModelConstants {

    /**
     * Regex to check if directory path fragment points to the model root
     */
    public static final String MODEL_ROOT_DIRECTORY_PATTERN = ".*root-directory.*";

    /**
     * The name of the logo file
     */
    public static final String PROCESS_LOGO_FILE_NAME = "processdefinition-logo.png";

    /**
     * Maximum file size in bytes for process logo image
     */
    public static final long PROCESS_LOGO_FILE_SIZE = 128 * 1024;

    /**
     * Allowed mime types for process logo image
     */
    public static final String[] PROCESS_LOGO_ALLOWED_MIME_TYPES = { "image/png" };

    /**
     * Default process logo for aperte process without own logo
     */
    public static final String PROCESS_LOGO_DEFAULT_RESOURCE = "/img/aperte-logo.png";
    
}
