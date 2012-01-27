package pl.net.bluesoft.rnd.pt.ext.vaadin;

import com.vaadin.ui.VerticalLayout;

public class HorizontalBox extends VerticalLayout {

    public HorizontalBox(String width) {
        setWidth(width);
    }
    
    public HorizontalBox(float width, int unit) {
        setWidth(width, unit);
    }

}
