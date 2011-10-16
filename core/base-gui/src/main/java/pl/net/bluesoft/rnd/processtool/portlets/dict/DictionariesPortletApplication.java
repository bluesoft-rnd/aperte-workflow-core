package pl.net.bluesoft.rnd.processtool.portlets.dict;

import pl.net.bluesoft.rnd.processtool.ui.dict.DictionariesMainPane;
import pl.net.bluesoft.rnd.util.vaadin.GenericVaadinPortlet2BpmApplication;

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
