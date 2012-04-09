package org.aperteworkflow.view.impl.history;

import org.aperteworkflow.ui.view.ViewRenderer;
import pl.net.bluesoft.rnd.util.func.Func;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ViewGeneratingFunction implements Func<ViewRenderer> {
    @Override
    public ViewRenderer invoke() {
        return new HistoryListPane();
    }
}
