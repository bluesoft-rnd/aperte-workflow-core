package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.ui.Upload;
import org.aperteworkflow.editor.signavio.ModelConstants;
import org.aperteworkflow.ui.base.BaseUploader;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessLogoUploader extends BaseUploader {
   
    private static final Logger logger = Logger.getLogger(ProcessLogoUploader.class.getName());
    
    private File logoFile;
    private ProcessLogoHandler processLogoHandler;

    public ProcessLogoUploader() {
        super(GenericEditorApplication.getCurrent());
        setMaxFileSize(ModelConstants.PROCESS_LOGO_FILE_SIZE);
        addAllowedMimeType(ModelConstants.PROCESS_LOGO_ALLOWED_MIME_TYPES);
    }

    public void setProcessLogoHandler(ProcessLogoHandler processLogoHandler) {
        this.processLogoHandler = processLogoHandler;
    }

    @Override
    protected OutputStream createOutputStream() throws Exception {
        logoFile = File.createTempFile("temp-aw-process-logo-", ".tmp");
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

        // notify the handler
        processLogoHandler.handleProcessLogo(logoFile);
    }

}
