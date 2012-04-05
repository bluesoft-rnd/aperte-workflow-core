package pl.net.bluesoft.rnd.pt.utils.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import pl.net.bluesoft.util.lang.Strings;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class OXHelper {
    public static class OXException extends RuntimeException {
        public OXException() {
            super();
        }

        public OXException(String message) {
            super(message);
        }

        public OXException(String message, Throwable cause) {
            super(message, cause);
        }

        public OXException(Throwable cause) {
            super(cause);
        }
    }

    protected abstract Class[] getSupportedClasses();

    public OXHelper() {
        Class[] classes = getSupportedClasses();
        xstream = new XStream();
        xstream.processAnnotations(classes);
        supportedClasses = new HashSet<Class>();
        Collections.addAll(supportedClasses, classes);
    }

    public void registerConverter(SingleValueConverter converter) {
        xstream.registerConverter(converter);
    }

    private XStream xstream;
    private Set<Class> supportedClasses;

    public String marshall(Object object) {
        if (object == null || !supportedClasses.contains(object.getClass())) {
            throw new IllegalArgumentException("Object of type: " + (object != null ? object.getClass() : "null") + " is not supported!");
        }
        return xstream.toXML(object);
    }

    public Object unmarshall(String xml) {
        if (!Strings.hasText(xml)) {
            throw new IllegalArgumentException("Cannot unmarshall an empty string!");
        }
        return xstream.fromXML(xml);
    }

    public Object unmarshall(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Cannot unmarshall an empty input stream!");
        }
        return xstream.fromXML(stream);
    }

    public static String replaceXmlEscapeCharacters(String input) {
        return input.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&#039;", "\'").replaceAll("&amp;", "&");
    }

    public static String removeCDATATag(String input) {
        return input.replaceAll("<!\\[CDATA\\[(.*?)\\]\\]>", "$1");
    }
}
