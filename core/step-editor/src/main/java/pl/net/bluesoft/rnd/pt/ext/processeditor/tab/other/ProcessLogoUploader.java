package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.ui.Upload;
import org.aperteworkflow.editor.signavio.ModelConstants;
import org.aperteworkflow.ui.base.BaseUploader;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class ProcessLogoUploader extends BaseUploader {
   
    private static final Logger logger = Logger.getLogger(ProcessLogoUploader.class.getName());
    
    private ByteArrayOutputStream processLogoOut;
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
        processLogoOut = new ByteArrayOutputStream();
        return processLogoOut;
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
        super.uploadSucceeded(event);

        // notify the handler
        processLogoHandler.handleProcessLogo(processLogoOut.toByteArray());

        // clean up
        processLogoOut = null;
    }

}
