package org.aperteworkflow.ui.view;

import pl.net.bluesoft.rnd.util.func.Func;

import java.util.Collection;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface ViewRegistry {

    Collection<ViewRenderer> getViews();
    void registerView(Func<ViewRenderer> v);

    void unregisterView(Func<ViewRenderer> v);
}
