package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;


import com.vaadin.ui.Upload;
import org.aperteworkflow.ui.base.BaseUploader;
import pl.net.bluesoft.rnd.pt.ext.signavio.ModelConstants;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessLogoUploader extends BaseUploader {
   
    private static final Logger logger = Logger.getLogger(ProcessLogoUploader.class.getName());
    
    private File logoFile;
    private String processDir;
    private ProcessLogoHandler processLogoHandler;

    public ProcessLogoUploader() {
        super(GenericEditorApplication.getCurrent()); // TODO remove after fixing this in baseuploader
        setMaxFileSize(25 * 1024);
        addAllowedMimeType("image/png");
    }

    public void setProcessLogoHandler(ProcessLogoHandler processLogoHandler) {
        this.processLogoHandler = processLogoHandler;
    }

    @Override
    protected OutputStream createOutputStream() throws Exception {
        logoFile = File.createTempFile("temp-logo-", ".tmp");
        logoFile.deleteOnExit();
        return new FileOutputStream(logoFile);
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        super.uploadSucceeded(event);

        if (logoFile == null) {
            logger.log(Level.SEVERE, "No process logo file");
            return;
        }

        File processLogoFile = new File(getProcessLogoFile());
        if (!logoFile.renameTo(processLogoFile)) {
            logger.log(Level.SEVERE, "Failed to move process logo file");
            VaadinUtility.errorNotification(application, messages, "proceslogoupload.move.failed");
        } else {
            processLogoHandler.handleProcessLogo(processLogoFile);
        }
    }

    public String getProcessDir() {
        return processDir;
    }

    public void setProcessDir(String processDir) {
        this.processDir = processDir;
    }
    
    public String getProcessLogoFile() {
        return processDir + File.separator + ModelConstants.LOGO_FILE_NAME;
    }
}
