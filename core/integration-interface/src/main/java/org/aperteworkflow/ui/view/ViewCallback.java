package org.aperteworkflow.ui.view;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ViewCallback {
    void displayProcessData(BpmTask task, boolean forward);
}
