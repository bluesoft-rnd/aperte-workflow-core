package org.aperteworkflow.ui.view.impl;

import org.aperteworkflow.ui.view.GenericPortletViewRenderer;
import org.aperteworkflow.ui.view.ViewRegistry;
import org.aperteworkflow.ui.view.ViewRenderer;
import pl.net.bluesoft.rnd.util.func.Func;

import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class DefaultViewRegistryImpl implements ViewRegistry {
    private Set<Func<ViewRenderer>> viewFunctions = new HashSet<Func<ViewRenderer>>();
	private Map<String, Set<GenericPortletViewRenderer>> genericPortletViewRenderers = new HashMap<String, Set<GenericPortletViewRenderer>>();

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

	@Override
	public synchronized Collection<GenericPortletViewRenderer> getGenericPortletViews(String portletKey) {
		return genericPortletViewRenderers.containsKey(portletKey)
				? genericPortletViewRenderers.get(portletKey)
				: Collections.<GenericPortletViewRenderer>emptyList();
	}

	@Override
	public synchronized void registerGenericPortletViewRenderer(String portletKey, GenericPortletViewRenderer renderer) {
		if (!genericPortletViewRenderers.containsKey(portletKey)) {
			genericPortletViewRenderers.put(portletKey, new HashSet<GenericPortletViewRenderer>());
		}
		genericPortletViewRenderers.get(portletKey).add(renderer);
	}

	@Override
	public synchronized void unregisterGenericPortletViewRenderer(String portletKey, GenericPortletViewRenderer renderer) {
		if (genericPortletViewRenderers.containsKey(portletKey)) {
			genericPortletViewRenderers.get(portletKey).remove(renderer);
		}
	}
}
