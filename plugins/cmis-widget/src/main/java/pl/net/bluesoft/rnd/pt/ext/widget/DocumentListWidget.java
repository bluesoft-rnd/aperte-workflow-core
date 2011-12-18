package pl.net.bluesoft.rnd.pt.ext.widget;

import java.util.Collection;

import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.pt.ext.cmis.widget.CmisDocumentListWidget;

public class DocumentListWidget extends BaseProcessToolWidget implements
        ProcessToolDataWidget, ProcessToolVaadinWidget {

    private VerticalLayout mainLayout;

    @Override
    public void addChild(ProcessToolWidget child) {
        throw new IllegalArgumentException("Not supported");
    }

    @Override
    public Component render() {
        mainLayout = new VerticalLayout();
        Button refreshDocumentList = new Button(getMessage("pt.ext.cmis.list.refresh"));
        refreshDocumentList.setIcon(new ClassResource(CmisDocumentListWidget.class, "/img/load-repository.png", getApplication()));
        refreshDocumentList.setImmediate(true);
        refreshDocumentList.setStyleName(BaseTheme.BUTTON_LINK);
        refreshDocumentList.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                reload();
            }
        });
        mainLayout.addComponent(refreshDocumentList);
        reload();
        return mainLayout;
    }

    private void reload() {

    }

    @Override
    public Collection<String> validateData(ProcessInstance processInstance) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveData(ProcessInstance processInstance) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadData(ProcessInstance processInstance) {
        // TODO Auto-generated method stub

    }

}