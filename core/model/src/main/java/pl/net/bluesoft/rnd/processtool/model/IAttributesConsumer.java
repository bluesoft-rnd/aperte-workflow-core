package pl.net.bluesoft.rnd.processtool.model;

/**
 * Created by pkuciapski on 2014-05-07.
 */
public interface IAttributesConsumer extends IAttributesProvider {
    void setSimpleAttribute(String key, String value);

    void setSimpleLargeAttribute(String key, String value);

    void addAttribute(Object attribute);

    void setAttribute(String key, Object attribute);
}
