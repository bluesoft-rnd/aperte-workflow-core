package pl.net.bluesoft.rnd.processtool.portlets.dict;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;
import pl.net.bluesoft.rnd.processtool.ui.dict.DictionariesMainPane;

public class DictionariesPortletApplication extends GenericVaadinPortlet2BpmApplication {

    public DictionariesPortletApplication() {
        loginRequired = true;
    }

    @Override
    protected void initializePortlet() {
        DictionariesMainPane mainPane = new DictionariesMainPane(this, this, this);
        getMainWindow().setContent(mainPane);
    }

    @Override
    protected void renderPortlet() {
    }
}
