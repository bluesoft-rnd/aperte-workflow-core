package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.pt.ext.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.io.File;
import java.util.Collection;

public class OtherTab extends VerticalLayout implements ProcessLogoHandler, DataHandler {

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

    @Override
    public void loadData() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void saveData() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<String> validateData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
