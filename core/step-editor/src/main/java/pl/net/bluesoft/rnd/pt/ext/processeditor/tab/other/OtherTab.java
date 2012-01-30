package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.File;

public class OtherTab extends VerticalLayout implements ProcessLogoHandler {

    private ProcessLogoUploader logoUploader;
     private Embedded logoImage;

    public OtherTab() {
        logoUploader = new ProcessLogoUploader();
        logoUploader.setProcessLogoHandler(this);
        addComponent(logoUploader);
    }
    
    public void setProcessDir(String dir) {
        logoUploader.setProcessDir(dir);
    }

    @Override
    public void handleProcessLogo(File processLogoFile) {
        if (logoImage != null) {
            removeComponent(logoImage);
            logoImage = null;
        }

        if (processLogoFile.exists()) {
            logoImage = VaadinUtility.embedded(GenericEditorApplication.getCurrent(), processLogoFile);
            logoImage.setWidth("75px");
            logoImage.setHeight("75px");
            addComponent(logoImage);
        } else {
            // TODO display notification
        }
    }

}
