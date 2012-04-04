package pl.net.bluesoft.rnd.processtool.ui.widgets;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.ui.WidgetContextSupport;

/**
 * @author amichalak@bluesoft.net.pl
 */
public interface ProcessToolActionCallback {
    void actionPerformed(ProcessStateAction action);
    void actionFailed(ProcessStateAction action);
    WidgetContextSupport getWidgetContextSupport();
}
