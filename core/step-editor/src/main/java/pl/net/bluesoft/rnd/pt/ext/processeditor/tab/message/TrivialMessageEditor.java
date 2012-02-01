package pl.net.bluesoft.rnd.pt.ext.processeditor.tab.message;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import pl.net.bluesoft.rnd.pt.ext.vaadin.DataHandler;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * very simple process editor based on TextArea component
 */
public class TrivialMessageEditor extends VerticalLayout implements DataHandler {

    private static final Logger logger = Logger.getLogger(TrivialMessageEditor.class.getName());
    
    private static final String PROPERTIES_ENCODING = "US-ASCII";
    private static final String EDITOR_ENCODING = "UTF-8";
    
    private String messagesContent;
    
    private TextArea messagesArea;
    private Label messagesDescriptionLabel;

    public TrivialMessageEditor() {
        I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
        
        messagesDescriptionLabel = new Label(messages.getMessage("messages.editor.description"));

        messagesArea = new TextArea();
        messagesArea.setRows(20);
        messagesArea.setNullRepresentation("");
        messagesArea.setWidth("100%");

        setSpacing(true);
        addComponent(messagesDescriptionLabel);
        addComponent(messagesArea);
    }

    @Override
    public void loadData() {
        if (messagesContent == null) {
            return;
        }

//        try {
            // decode from base64
            byte[] decoded = Base64.decodeBase64(messagesContent);
            String utf8 = StringUtils.newStringUtf8(decoded);




            messagesArea.setValue(utf8);
//        } catch (UnsupportedEncodingException e) {
//            logger.log(Level.SEVERE, "Error decoding the base64 messages content", e);
//        }
    }

    @Override
    public void saveData() {
//        try {
            String utf8 = (String) messagesArea.getValue();
            byte[] encoded = Base64.encodeBase64(utf8.getBytes());
            messagesContent = StringUtils.newStringUtf8(encoded);
//        } catch (UnsupportedEncodingException e) {
//            logger.log(Level.SEVERE, "Error decoding the base64 messages content", e);
//        }
    }

    @Override
    public Collection<String> validateData() {
        return null;
    }

    public String getMessagesContent() {
        return messagesContent;
    }

    public void setMessagesContent(String messagesContent) {
        this.messagesContent = messagesContent;
    }
}
