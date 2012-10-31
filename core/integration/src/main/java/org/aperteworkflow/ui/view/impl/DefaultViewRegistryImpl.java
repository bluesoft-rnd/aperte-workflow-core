package org.aperteworkflow.ui.view.impl;

import org.aperteworkflow.ui.view.ViewRegistry;
import org.aperteworkflow.ui.view.ViewRenderer;
import pl.net.bluesoft.rnd.util.func.Func;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class DefaultViewRegistryImpl implements ViewRegistry { 
    private Set<Func<ViewRenderer>> viewFunctions = new HashSet<Func<ViewRenderer>>();
    @Override
    public synchronized Collection<ViewRenderer> getViews() {
        Set<ViewRenderer> res = new HashSet<ViewRenderer>();
        for (Func<ViewRenderer> f : viewFunctions) {
            res.add(f.invoke());
        }
        return res;
    }

    @Override
    public synchronized void registerView(Func<ViewRenderer> v) {
        viewFunctions.add(v);
    }

    @Override
    public synchronized void unregisterView(Func<ViewRenderer> v) {
        viewFunctions.remove(v);
    }
}
