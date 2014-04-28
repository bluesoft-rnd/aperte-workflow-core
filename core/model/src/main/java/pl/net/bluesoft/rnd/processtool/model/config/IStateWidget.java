package pl.net.bluesoft.rnd.processtool.model.config;

import java.util.Set;

/**
 * Created by pkuciapski on 2014-04-25.
 */
public interface IStateWidget {
    Long getId();

    Set<? extends IStateWidgetAttribute> getAttributes();

    IStateWidgetAttribute getAttributeByName(String name);

    Set<? extends IStateWidget> getChildren();

    String getClassName();

    Integer getPriority();

    Set<? extends IPermission> getPermissions();
}
