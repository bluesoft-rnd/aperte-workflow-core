package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import com.vaadin.data.Property;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Upload.*;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceAttachmentAttribute;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class FileUploadComponent extends CustomComponent implements StartedListener, FailedListener, SucceededListener,
        ProgressListener, FinishedListener, Receiver, Property {

    public static final String UPLOAD_CANCEL = "fileupload.cancel";
    public static final String UPLOAD_START = "fileupload.start";
    public static final String UPLOAD_DOWNLOAD = "fileupload.download";
    public static final String UPLOAD_CHOOSE_FILE = "fileupload.choose";
    public static final String UPLOAD_FILE_NAME = "fileupload.filename";
    public static final String UPLOAD_PROGRESS = "fileupload.progress";
    public static final String PROGRESS_PROCESSED = "fileupload.progress.processed";
    public static final String PROGRESS_FAILED = "fileupload.progress.failed";

    private I18NSource i18NSource;

    private ProgressIndicator pi = new ProgressIndicator();

    private Upload uploadFile = new Upload(null, this);
    private Button downloadFile = new Button();
    private Button cancelProcessing = new Button();

    private Label textualProgress = new Label();
    private Label fileName = new Label();
    private Label failedMessage = new Label();

    private FormLayout mainPanel = new FormLayout();

    private HorizontalLayout stateLayout;

    private ProcessInstanceAttachmentAttribute instanceAttachment;

    private ByteArrayOutputStream baos = null;
    private String receivedFileName;
    private String receivedMimeType;

    public FileUploadComponent(I18NSource i18NSource) {
        this.i18NSource = i18NSource;

        uploadFile.addStyleName("default");
        uploadFile.setButtonCaption(i18NSource.getMessage(UPLOAD_START));
        uploadFile.addListener((FailedListener) this);
        uploadFile.addListener((FinishedListener) this);
        uploadFile.addListener((ProgressListener) this);
        uploadFile.addListener((StartedListener) this);
        uploadFile.addListener((SucceededListener) this);

        mainPanel.setMargin(true);

        initView();

        setCompositionRoot(mainPanel);
    }

    @Override
    public Object getValue() {
        if (baos != null && StringUtil.hasText(receivedFileName) && StringUtil.hasText(receivedMimeType)) {
            if (instanceAttachment == null) {
                instanceAttachment = new ProcessInstanceAttachmentAttribute();
            }
            instanceAttachment.setData(baos.toByteArray());
            instanceAttachment.setFileName(receivedFileName);
            instanceAttachment.setMimeType(receivedMimeType);
        }
        return instanceAttachment;
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        if (newValue != null && !(newValue instanceof ProcessInstanceAttachmentAttribute)) {
            throw new ConversionException("Cannot convert " + newValue.getClass() + " to " + ProcessInstanceAttachmentAttribute.class);
        }
        if (isReadOnly()) {
            throw new ReadOnlyException("Property is readonly!");
        }
        instanceAttachment = (ProcessInstanceAttachmentAttribute) newValue;
        if (instanceAttachment != null && StringUtil.hasText(instanceAttachment.getFileName())) {
            fileName.setValue(instanceAttachment.getFileName());
        }
        downloadFile.setEnabled(canDownload());
    }

    @Override
    public Class<?> getType() {
        return ProcessInstanceAttachmentAttribute.class;
    }

    private boolean canDownload() {
        return getValue() != null && instanceAttachment.getData() != null && StringUtil.hasText(instanceAttachment.getFileName())
                && StringUtil.hasText(instanceAttachment.getMimeType());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        uploadFile.setVisible(!readOnly);
        super.setReadOnly(readOnly);
    }

    private void initView() {
        uploadFile.setImmediate(true);

        downloadFile.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (instanceAttachment != null && instanceAttachment.getData() != null) {
                    StreamResource.StreamSource source = new StreamResource.StreamSource() {
                        @Override
                        public InputStream getStream() {
                            return new ByteArrayInputStream(instanceAttachment.getData());
                        }
                    };
                    StreamResource resource = new StreamResource(source, instanceAttachment.getFileName(), getApplication());
                    resource.setMIMEType(instanceAttachment.getMimeType());
                    getApplication().getMainWindow().open(resource, "_new");
                }
            }
        });
        downloadFile.addStyleName("default");
        downloadFile.setEnabled(false);
        downloadFile.setStyleName("small");
        downloadFile.setCaption(i18NSource.getMessage(UPLOAD_DOWNLOAD));

        cancelProcessing.addStyleName("default");
        cancelProcessing.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                uploadFile.interruptUpload();
            }
        });
        cancelProcessing.setCaption(i18NSource.getMessage(UPLOAD_CANCEL));
        cancelProcessing.setStyleName("small");

        HorizontalLayout fileLayout = new HorizontalLayout();
        fileLayout.setSpacing(true);
        fileLayout.addComponent(new Label("<b>" + i18NSource.getMessage(UPLOAD_FILE_NAME) + "</b>", Label.CONTENT_RAW));
        fileLayout.addComponent(fileName);
        fileLayout.addComponent(uploadFile);
        fileLayout.addComponent(downloadFile);
        fileName.setValue(i18NSource.getMessage(UPLOAD_CHOOSE_FILE));

        mainPanel.addComponent(fileLayout);

        stateLayout = new HorizontalLayout();
        stateLayout.setSpacing(true);
        stateLayout.addComponent(new Label("<b>" + i18NSource.getMessage(UPLOAD_PROGRESS) + "</b>", Label.CONTENT_RAW));
        stateLayout.addComponent(pi);
        stateLayout.addComponent(cancelProcessing);

        for (Iterator<Component> it = stateLayout.getComponentIterator(); it.hasNext();) {
            stateLayout.setComponentAlignment(it.next(), Alignment.MIDDLE_CENTER);
        }

        for (Iterator<Component> it = fileLayout.getComponentIterator(); it.hasNext();) {
            fileLayout.setComponentAlignment(it.next(), Alignment.MIDDLE_CENTER);
        }
    }

    @Override
    public void uploadStarted(StartedEvent event) {
        pi.setValue(0f);
        pi.setPollingInterval(500);
        fileName.setValue(event.getFilename());
        mainPanel.addComponent(stateLayout);
        mainPanel.addComponent(textualProgress);
        mainPanel.removeComponent(failedMessage);
        downloadFile.setEnabled(false);
    }

    @Override
    public void uploadFailed(FailedEvent event) {
        baos = null;
        failedMessage.setValue("<b>" + i18NSource.getMessage(PROGRESS_FAILED).replaceFirst("%s", "" + Math.round(100 * (Float) pi.getValue())) + "</b>");
        mainPanel.addComponent(failedMessage);
        downloadFile.setEnabled(canDownload());
    }

    @Override
    public void updateProgress(long readBytes, long contentLength) {
        pi.setValue(new Float(readBytes / (float) contentLength));
        textualProgress.setValue(i18NSource.getMessage(PROGRESS_PROCESSED).replaceFirst("%s", "" + readBytes).replaceFirst("%s", "" + contentLength));
    }

    @Override
    public void uploadSucceeded(SucceededEvent event) {
        downloadFile.setEnabled(canDownload());
    }

    @Override
    public void uploadFinished(FinishedEvent event) {
        mainPanel.removeComponent(stateLayout);
        mainPanel.removeComponent(textualProgress);
    }

    @Override
    public OutputStream receiveUpload(String filename, String mimeType) {
        receivedFileName = filename;
        receivedMimeType = mimeType;
        return baos = new ByteArrayOutputStream();
    }
}
