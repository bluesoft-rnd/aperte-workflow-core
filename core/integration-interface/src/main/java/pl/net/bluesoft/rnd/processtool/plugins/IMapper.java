package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * Created by pkuciapski on 2014-05-08.
 */
public interface IMapper<T extends IAttributesProvider> {
    void map(IAttributesConsumer consumer, T provider);
}
