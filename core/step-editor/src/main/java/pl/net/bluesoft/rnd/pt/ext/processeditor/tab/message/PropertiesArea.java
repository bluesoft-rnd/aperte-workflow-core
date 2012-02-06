package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.message;


import com.vaadin.ui.TextArea;
import org.aperteworkflow.editor.domain.Language;

public class PropertiesArea extends TextArea {

    private Language language;

    public PropertiesArea() {
        setNullRepresentation("");
        setRows(20);
        setWidth("100%");
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
