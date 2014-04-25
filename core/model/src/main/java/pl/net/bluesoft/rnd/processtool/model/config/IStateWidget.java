package pl.net.bluesoft.rnd.processtool.model.config;

import java.util.Set;

/**
 * Created by pkuciapski on 2014-04-25.
 */
public interface IStateWidget {
    Long getId();

    Set<ProcessStateWidgetAttribute> getAttributes();

    ProcessStateWidgetAttribute getAttributeByName(String name);

    Set<ProcessStateWidget> getChildren();

    String getClassName();

    Integer getPriority();
}
