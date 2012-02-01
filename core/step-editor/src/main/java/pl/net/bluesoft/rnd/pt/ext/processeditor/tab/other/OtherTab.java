package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import org.aperteworkflow.editor.signavio.ModelConstants;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.File;
import java.util.Collection;

public class OtherTab extends GridLayout implements ProcessLogoHandler, DataHandler {

    private String processDir;
    private String processPrefix;
    
    private ProcessLogoUploader logoUploader;
    private Embedded logoImage;
    private Label logoDescriptionLabel;

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
        addComponent(logoUploader, 1, 1);

        setComponentAlignment(logoUploader, Alignment.MIDDLE_LEFT);

        setColumnExpandRatio(0, 0);
        setColumnExpandRatio(1, 1);
    }
    
    public void setProcessDir(String processDir) {
        this.processDir = processDir;
    }

    @Override
    public void handleProcessLogo(File processLogoFile) {
        if (logoImage != null) {
            removeComponent(logoImage);
            logoImage = null;
        }

        if (processLogoFile.exists()) {
            logoImage = VaadinUtility.embedded(
                    GenericEditorApplication.getCurrent(),
                    processLogoFile
            );
        } else {
            logoImage = VaadinUtility.embedded(
                    GenericEditorApplication.getCurrent(),
                    ModelConstants.PROCESS_LOGO_DEFAULT_RESOURCE
            );
        }

        if (logoImage != null) {
            logoImage.setWidth("32px");
            logoImage.setHeight("32px");
            addComponent(logoImage, 0, 1);
        }
    }

    @Override
    public File getProcessLogoFile() {
        return new File(processDir + File.separator + ModelConstants.PROCESS_LOGO_FILE_NAME);
    }

    @Override
    public void loadData() {
        // try to load the image
        handleProcessLogo(getProcessLogoFile());
    }

    @Override
    public void saveData() {

    }

    @Override
    public Collection<String> validateData() {
        return null;
    }
}
