package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.IAttribute;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * Created by pkuciapski on 2014-05-07.
 */
public interface IAttributesMapper<T extends IAttribute> {
    void map(T attribute, IAttributesConsumer consumer, IAttributesProvider provider);
}
