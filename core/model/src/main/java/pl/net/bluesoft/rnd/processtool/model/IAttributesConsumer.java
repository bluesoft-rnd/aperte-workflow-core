package pl.net.bluesoft.rnd.processtool.model;

/**
 * Created by pkuciapski on 2014-05-07.
 */
public interface IAttributesConsumer {
    void setSimpleAttribute(String key, String value);

    void addAttribute(Object attribute);
}
