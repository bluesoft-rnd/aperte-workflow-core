package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import org.aperteworkflow.editor.domain.ProcessModelConfig;
import org.aperteworkflow.editor.signavio.ModelConstants;
import org.aperteworkflow.editor.signavio.ModelUtils;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Editor for process logo image that stores it as separate file in the modeler repository directory
 */
public class ProcessLogoEditor extends GridLayout implements ProcessLogoHandler, DataHandler {

    private static final Logger logger = Logger.getLogger(OtherTab.class.getName());

    private ProcessModelConfig processModelConfig;

    private boolean processModelUnsaved;
    private Label processModelUnsavedLabel;

    private File logoFile;
    private Embedded logoImage;
    private ProcessLogoUploader logoUploader;
    private Button logoResetButton;
    private Label logoDescriptionLabel;

    public ProcessLogoEditor() {
        super(3, 2);
        initComponent();
        initLayout();
    }

    public void setProcessModelConfig(ProcessModelConfig processModelConfig) {
        this.processModelConfig = processModelConfig;
    }

    @Override
    public void handleProcessLogo(File processLogoFile) {
        logoFile = processLogoFile;

        if (logoFile != null && logoFile.exists()) {
            showProcessLogo(logoFile);
        } else {
            showDefaultProcessLogo();
        }
    }

    private void showProcessLogo(File processLogoFile) {
        if (logoImage != null) {
            removeComponent(logoImage);
            logoImage = null;
        }

        try {
            final FileInputStream fis = new FileInputStream(processLogoFile);

            StreamResource.StreamSource source = new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    return fis;
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
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to read the process logo file", e);
        }
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

        // ugly HTML formatting, this should be moved to external CSS
        processModelUnsavedLabel = new Label("<span style=\"color: red; font-weight: bold\">" + messages.getMessage("process.model.unsaved") + "</span>");
        processModelUnsavedLabel.setContentMode(Label.CONTENT_XHTML);

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

    }

    @Override
    public File getProcessLogoFile() {
        return new File(ModelUtils.getModelLogoFilePath(processModelConfig));
    }

    @Override
    public void loadData() {
        processModelUnsaved = processModelConfig.isNewModel();
        if (processModelUnsaved) {
            // The model is not saved, display the error and stop loading
            addComponent(processModelUnsavedLabel, 0, 1, 2, 1);
            return;
        }

        // it's important that upload component is the last one in the row
        // otherwise the layout gets messed up
        addComponent(logoResetButton, 1, 1);
        addComponent(logoUploader, 2, 1);

        setColumnExpandRatio(2, 1);

        setComponentAlignment(logoUploader, Alignment.MIDDLE_LEFT);
        setComponentAlignment(logoResetButton, Alignment.MIDDLE_LEFT);

        // try to load the image
        handleProcessLogo(getProcessLogoFile());
    }

    @Override
    public void saveData() {
        if (processModelUnsaved) {
            return;
        }

        File processLogoFile = getProcessLogoFile();
        if (logoFile == null) {
            if (processLogoFile.exists() && !processLogoFile.delete()) {
                logger.log(Level.SEVERE, "Failed to delete logo file " + processLogoFile);

                VaadinUtility.errorNotification(
                        GenericEditorApplication.getCurrent(),
                        I18NSource.ThreadUtil.getThreadI18nSource(),
                        "process.logo.delete.failed"
                );
            }
        } else if (!logoFile.equals(processLogoFile)) {
            if (!logoFile.renameTo(processLogoFile)) {
                logger.log(Level.SEVERE, "Failed to move process logo file from " + logoFile + " to " + processLogoFile);

                VaadinUtility.errorNotification(
                        GenericEditorApplication.getCurrent(),
                        I18NSource.ThreadUtil.getThreadI18nSource(),
                        "process.logo.move.failed"
                );
            }
        }
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

}
