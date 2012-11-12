package org.aperteworkflow.editor.processeditor.tab.other;

import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.ProcessConfig;
import org.aperteworkflow.editor.signavio.ModelConstants;
import org.aperteworkflow.editor.vaadin.DataHandler;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.pt.utils.lang.Lang2;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

/**
 * Editor for process logo image. Logo content is stored in {@link ProcessConfig} object.
 */
public class ProcessLogoEditor extends GridLayout implements ProcessLogoHandler, DataHandler {

    private ProcessConfig processConfig;

    private byte[] logoContent;
    private Embedded logoImage;
    private ProcessLogoUploader logoUploader;
    private Button logoResetButton;
    private Label logoDescriptionLabel;

    public ProcessLogoEditor() {
        super(3, 2);
        initComponent();
        initLayout();
    }

    public void setProcessConfig(ProcessConfig processConfig) {
        this.processConfig = processConfig;
    }

    @Override
    public void handleProcessLogo(byte[] content) {
        logoContent = Lang2.noCopy(content);

        if (logoContent != null) {
            showProcessLogo(logoContent);
        } else {
            showDefaultProcessLogo();
        }
    }

    private void showProcessLogo(final byte[] content) {
        if (logoImage != null) {
            removeComponent(logoImage);
            logoImage = null;
        }

        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(content);
            }
        };

        // generate random file name to bypass web browser image cache
        String randomFileName = "process-logo-" + System.nanoTime() + ".png";
        StreamResource resource = new StreamResource(source, randomFileName, GenericEditorApplication.getCurrent());

        // show the logo
        logoImage = new Embedded();
        logoImage.setType(Embedded.TYPE_IMAGE);
        logoImage.setSource(resource);
        logoImage.setWidth("32px");
        logoImage.setHeight("32px");
        addComponent(logoImage, 0, 1);

        // allow to reset the logo
        logoResetButton.setEnabled(true);
    }

    private void showDefaultProcessLogo() {
        if (logoImage != null) {
            removeComponent(logoImage);
            logoImage = null;
        }

        // show the default logo
        logoImage = VaadinUtility.embedded(
                GenericEditorApplication.getCurrent(),
                ModelConstants.PROCESS_LOGO_DEFAULT_RESOURCE
        );
        logoImage.setWidth("32px");
        logoImage.setHeight("32px");
        addComponent(logoImage, 0, 1);

        // no point to reset the default logo
        logoResetButton.setEnabled(false);
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        logoUploader = new ProcessLogoUploader();
        logoUploader.setProcessLogoHandler(this);

        logoDescriptionLabel = new Label(messages.getMessage(
                "process.logo.description",
                new Object[] { humanReadableByteCount(ModelConstants.PROCESS_LOGO_FILE_SIZE, false) }
        ));

        logoResetButton = new Button(messages.getMessage("process.logo.reset"));
        logoResetButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                handleProcessLogo(null);
            }
        });
    }

    // TODO create formatter class
    private String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void initLayout() {
        setSpacing(true);
        setWidth("100%");

        addComponent(logoDescriptionLabel, 0, 0, 2, 0);

        // it's important for upload component to be the last one in the row
        // otherwise the layout gets messed up
        addComponent(logoResetButton, 1, 1);
        addComponent(logoUploader, 2, 1);

        setColumnExpandRatio(2, 1);

        setComponentAlignment(logoUploader, Alignment.MIDDLE_LEFT);
        setComponentAlignment(logoResetButton, Alignment.MIDDLE_LEFT);
    }

    @Override
    public void loadData() {
        logoContent = processConfig.getProcessIcon();

        // load the initial icon
        handleProcessLogo(logoContent);
    }

    @Override
    public void saveData() {
       processConfig.setProcessIcon(logoContent);
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

}
