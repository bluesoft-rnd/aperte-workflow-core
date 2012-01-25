package pl.net.bluesoft.rnd.pt.ext.processeditor.tab;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

public abstract class AbstractTab extends CustomComponent {

    public abstract Component getContent();

    public AbstractTab init() {
        setCompositionRoot(getContent());
        return this;
    }

}
