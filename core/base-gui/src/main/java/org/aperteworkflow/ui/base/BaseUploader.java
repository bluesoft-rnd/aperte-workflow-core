package org.aperteworkflow.ui.base;

import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base uploader designed to provide common functionality for all Aperte Workflow Vaadin upload components
 */
public abstract class BaseUploader extends VerticalLayout implements Upload.SucceededListener, Upload.FailedListener,
        Upload.Receiver, Upload.ProgressListener {

    private static final Logger logger = Logger.getLogger(BaseUploader.class.getName());
    
    private long maxFileSize;
    private Set<String> allowedMimeTypes;
    private final OutputStream nullOut;
    private String uploadFailedNotification;

    protected OutputStream out;
    protected Upload upload;
    protected ProgressIndicator progressIndicator;
    protected VerticalLayout mainLayout;

    public BaseUploader() {
        nullOut = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
        maxFileSize = 1024;
        allowedMimeTypes = new HashSet<String>();

        initComponent();
    }

    private void initComponent() {
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPollingInterval(100);
        progressIndicator.setSizeFull();
        resetAndHideProgressIndicator();

        upload = new Upload(null, this);
        upload.setImmediate(true);
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
        upload.addListener((Upload.ProgressListener) this);

        addComponent(upload);
        addComponent(progressIndicator);
    }

    @Override
    public void updateProgress(long readBytes, long contentLength) {
        if (maxFileSize > 0 && (maxFileSize < readBytes || (contentLength != -1 && maxFileSize < contentLength))) {
            I18NSource messages = VaadinUtility.getThreadI18nSource();
            setUploadFailedNotification(messages.getMessage("baseuploader.filesize.exceeded"));
            upload.interruptUpload();
        }

        float progress = ((float) readBytes) / ((float) contentLength);
        if (progress > 1) {
            progress = 1;
        }
        progressIndicator.setValue(progress);

        //progressIndicator.setCaption(readableIn + " / " + readableAll);
    }

    @Override
    public void uploadFailed(Upload.FailedEvent event) {
        cleanup();

        if (uploadFailedNotification != null) {
            VaadinUtility.errorNotification(uploadFailedNotification);
            uploadFailedNotification = null;
        } else if (event.getReason() != null) {
            VaadinUtility.errorNotification(event.getReason().getMessage());
        }
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        out = null;

        I18NSource message = VaadinUtility.getThreadI18nSource();

        if (!allowedMimeTypes.isEmpty() && !allowedMimeTypes.contains(mimeType)) {
            logger.log(Level.INFO, "Disallowed mimeType " + mimeType);
            setUploadFailedNotification(message.getMessage("baseuploader.mimeType.disallowed"));
        } else {
            try {
                out = createOutputStream();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to create output stream", e);
                setUploadFailedNotification(message.getMessage("baseuploader.out.failed"));
            }
        }
        
        if (out == null) {
            // No valid output, interrupt and return fake
            upload.interruptUpload();
            return nullOut;
        } else {
            return out;
        }
    }

    @Override
    public void uploadSucceeded(Upload.SucceededEvent event) {
         cleanup();
    }

    private void cleanup() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // do nothing
            }
            out = null;
        }

        resetAndHideProgressIndicator();
    }

    protected OutputStream createOutputStream() throws Exception {
        return new FileOutputStream("/tmp");
    }

    protected void resetAndHideProgressIndicator() {
        progressIndicator.setValue(0);
        progressIndicator.setVisible(false);
    }

    protected void resetAndShowProgressIndicator() {
        progressIndicator.setValue(0);
        progressIndicator.setVisible(true);
    }

    protected void setUploadFailedNotification(String uploadFailedNotification) {
        this.uploadFailedNotification = uploadFailedNotification;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public void addAllowedMimeType(String... mimeTypes) {
        allowedMimeTypes.addAll(Arrays.asList(mimeTypes));
    }

    public void setUploadImmediate(boolean immediate) {
        upload.setImmediate(immediate);
    }

}
