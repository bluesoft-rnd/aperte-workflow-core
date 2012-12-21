package pl.net.bluesoft.rnd.processtool.view.impl;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.VerticalLayout;

import org.aperteworkflow.ui.view.ViewController;
import org.aperteworkflow.ui.view.ViewListener;
import org.aperteworkflow.ui.view.ViewRenderer;

import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityQueuesPane;
import pl.net.bluesoft.util.lang.TaskWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicViewController implements ViewController {
	private final String baseViewId;
	private final Map<String, ViewRenderer> rendererMap = new HashMap<String, ViewRenderer>();
	private final AbstractOrderedLayout viewContainer = new VerticalLayout();

	private String currentViewId;
	private Map<String, ?> currentViewData;

	private LinkedList<String> historyViewIds = new LinkedList<String>();
	private Map<String, Map<String, ?>> historyViewData = new HashMap<String, Map<String, ?>>();
	private List<ViewListener> listeners = new ArrayList<ViewListener>();
	private static final Logger logger = Logger.getLogger(BasicViewController.class.getName());

	public BasicViewController(ViewRenderer baseViewRenderer) {
		viewContainer.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		baseViewId = baseViewRenderer.getViewId();
		rendererMap.put(baseViewId, baseViewRenderer);
		currentViewId = baseViewId;
	}

	public ComponentContainer getViewContainer() {
		return viewContainer;
	}

	public Set<String> getViewIds() {
		return rendererMap.keySet();
	}

	public void displayView(Class<?> clazz) {
		displayView(clazz.getName(), null);
	}

	public void displayView(Class<?> clazz, Map<String, ?> viewData) {
		displayView(clazz.getName(), viewData);
	}

    public void displayView(Class<?> clazz, Map<String, ?> viewData, boolean forward) {
        displayView(clazz.getName(), viewData, forward);
    }

    @Override
	public void displayCurrentView() {
		renderView(currentViewId, null);
	}

	@Override
	public void displayView(String viewId) {
		displayView(viewId, null);
	}

	@Override
	public void displayView(String viewId, Map<String, ?> viewData) {
		displayView(viewId, viewData, false); // false? doskonały strzał milordzie - teraz w ogóle nie ma historii widoków
	}

	@Override
	public void displayPreviousView() {
		String previousViewId = getPreviousViewId();
		displayView(previousViewId, getHistoryViewData(previousViewId), false);
	}

    @Override
	public void displayView(String viewId, Map<String, ?> viewData, boolean forward) {
		handleViewHistory(viewId, forward);
		renderView(viewId, viewData);
	}

	private void renderView(final String viewId, Map<String, ?> viewData) {

		TaskWatch watch = new TaskWatch("Rendering view " + viewId);
		final ViewRenderer renderer = rendererMap.get(viewId);
		if (renderer == null) {
			throw new IllegalArgumentException("Unable to find view id: " + viewId);
		}
		currentViewData = viewData != null ? viewData : new HashMap<String, Object>();
		Component comp = null;
		try {
			comp = watch.watchTask("Really rendering", new Callable<Component>() {

				@Override
				public Component call() throws Exception {
					return renderer.render(currentViewData);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		comp.setWidth(100, Sizeable.UNITS_PERCENTAGE);
		viewContainer.removeAllComponents();
		viewContainer.addComponent(comp);
		viewContainer.setExpandRatio(comp, 1.0f);
		currentViewId = viewId;
		
		try {
			watch.watchTask("Bragging about it", new Callable() {

				@Override
				public Object call() throws Exception {
					fireViewChangedEvent(viewId);
					return null;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		watch.stopAll();
		logger.log(Level.INFO, watch.printSummary());
	}

	private void fireViewChangedEvent(String viewId) {
		for (ViewListener listener : listeners) {
			listener.viewChanged(viewId);
		}
	}

	private void handleViewHistory(String viewId, boolean forward) {
		if (viewId.equals(baseViewId)) {
			historyViewIds.clear();
			historyViewData.clear();
		}
		else {
			if (forward) {
				historyViewIds.addLast(currentViewId);
				historyViewData.put(currentViewId, currentViewData);
			}
			else {
				if(historyViewIds.size() > 0){
					historyViewData.remove(historyViewIds.removeLast());
				}
			}
		}
	}

	private String getPreviousViewId() {
		return historyViewIds.isEmpty() ? baseViewId : historyViewIds.getLast();
	}

	private Map<String, ?> getHistoryViewData(String viewId) {
		return historyViewData.containsKey(viewId) ? historyViewData.get(viewId) : new HashMap<String, Object>();
	}

	@Override
	public void addView(ViewRenderer renderer) {
		if (baseViewId.equals(renderer.getViewId())) {
			throw new IllegalArgumentException("Cannot overwrite base view identified by " + baseViewId);
		}
		rendererMap.put(renderer.getViewId(), renderer);
	}

	@Override
	public void removeView(String viewId) {
		if (baseViewId.equals(viewId)) {
			throw new IllegalArgumentException("Cannot remove base view identified by " + viewId);
		}
		rendererMap.remove(viewId);
	}

	@Override
	public void addViewListener(ViewListener viewListener) {
		listeners.add(viewListener);
	}

	@Override
	public void removeViewListener(ViewListener viewListener) {
		listeners.remove(viewListener);
	}

	@Override
	public void refreshCurrentView() {
		if (currentViewId != null && rendererMap.containsKey(currentViewId)) {
			ViewRenderer renderer = rendererMap.get(currentViewId);
			renderer.refreshData();
		}
	}

	public String getCurrentViewId() {
		return currentViewId;
	}
	public Map<String, ?> getCurrentViewData() {
		return currentViewData;
	}
}
