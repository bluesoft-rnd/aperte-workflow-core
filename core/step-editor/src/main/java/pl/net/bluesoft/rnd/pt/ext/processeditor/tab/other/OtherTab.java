package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.other;

import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;

public class OtherTab extends VerticalLayout {

    private ProcessLogoUploader logoUploader;

    private Embedded logo;

    public OtherTab() {
        logoUploader = new ProcessLogoUploader();
        
        addComponent(logoUploader);
    }
    
    public void setProcessDir(String dir) {
        logoUploader.setProcessDir(dir);
    }
}
