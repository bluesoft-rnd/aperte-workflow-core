package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import java.io.File;

public interface ProcessLogoHandler {

    /**
     * React to new process process logo
     * @param processLogoFile The file that points to the logo
     */
    void handleProcessLogo(File processLogoFile);

    /**
     * Get the file which is going to be used for storing the image
     * @return File
     */
    File getProcessLogoFile();
    
}
