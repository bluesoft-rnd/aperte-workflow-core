package pl.net.bluesoft.rnd.processtool.ui.widgets;

import com.vaadin.Application;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ProcessToolVaadinActionButton extends ProcessToolActionButton{
	void setApplication(Application application);
	void setI18NSource(I18NSource i18NSource);
}
