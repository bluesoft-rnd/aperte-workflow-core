package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
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

public class OtherTab extends GridLayout implements ProcessLogoHandler, DataHandler {

    private static final Logger logger = Logger.getLogger(OtherTab.class.getName());
    
    private ProcessModelConfig processModelConfig;
    
    private File logoFile;
    private ProcessLogoUploader logoUploader;
    private Embedded logoImage;
    private Label logoDescriptionLabel;
    private Label unsavedModelLabel;

    private boolean unsavedModel;

    public OtherTab() {
        super(2, 2);
        initComponent();
        initLayout();
    }

    private void initComponent() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();

        logoUploader = new ProcessLogoUploader();
        logoUploader.setProcessLogoHandler(this);

        logoDescriptionLabel = new Label(messages.getMessage(
                "process.logo.description",
                new Object[] { humanReadableByteCount(ModelConstants.PROCESS_LOGO_FILE_SIZE, false) }
        ));
        
        unsavedModelLabel = new Label("<b>" + messages.getMessage("process.model.unsaved") + "</b>");
        unsavedModelLabel.setContentMode(Label.CONTENT_XHTML);
    }

    // TODO create formatter class
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void initLayout() {
        setSpacing(true);
        setMargin(true);
        setWidth("100%");

        addComponent(logoDescriptionLabel, 0, 0, 1, 0);

        setColumnExpandRatio(0, 0);
        setColumnExpandRatio(1, 1);
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

            logoImage = new Embedded();
            logoImage.setType(Embedded.TYPE_IMAGE);
            logoImage.setSource(resource);
            logoImage.setWidth("32px");
            logoImage.setHeight("32px");
            addComponent(logoImage, 0, 1);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to read the process logo file", e);
        }
    }

    private void showDefaultProcessLogo() {
        if (logoImage != null) {
            removeComponent(logoImage);
            logoImage = null;
        }


        logoImage = VaadinUtility.embedded(
                GenericEditorApplication.getCurrent(),
                ModelConstants.PROCESS_LOGO_DEFAULT_RESOURCE
        );

        logoImage.setWidth("32px");
        logoImage.setHeight("32px");
        addComponent(logoImage, 0, 1);
    }

    @Override
    public File getProcessLogoFile() {
        return new File(ModelUtils.getModelLogoFilePath(processModelConfig));
    }

    @Override
    public void loadData() {
        unsavedModel = processModelConfig.isNewModel();
        if (unsavedModel) {
            // The model is not saved, display the error and stop loading
            addComponent(unsavedModelLabel, 0, 1, 1, 1);
            return;
        }

        addComponent(logoUploader, 1, 1);
        setComponentAlignment(logoUploader, Alignment.MIDDLE_LEFT);

        // try to load the image
        handleProcessLogo(getProcessLogoFile());
    }

    @Override
    public void saveData() {
        if (unsavedModel) {
            return;
        }

        File processLogoFile = getProcessLogoFile();
        if (logoFile.equals(processLogoFile)) {
            // no new logo file to save
            return;
        }

        if (!logoFile.renameTo(processLogoFile)) {
            logger.log(Level.SEVERE, "Failed to move process logo file from " + logoFile + " to " + processLogoFile);

            VaadinUtility.errorNotification(
                    GenericEditorApplication.getCurrent(),
                    I18NSource.ThreadUtil.getThreadI18nSource(),
                    "process.logo.move.failed"
            );
        }
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
}
