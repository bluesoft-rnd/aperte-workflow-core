package org.aperteworkflow.widgets.refresherwrapper.widgetset.client.ui;

import java.util.ArrayList;
import java.util.Set;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VCaption;
import com.vaadin.terminal.gwt.client.VCaptionWrapper;
import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Client side implementation of LazyLoadWrapper. The wrapper creates a
 * placeholder with a spinner on the view and notifies the server or loads the
 * lazy load component when the placeholder is visible.
 * 
 * When the wrapper receives the lazy load component from the server, the
 * wrapper replaces the placeholder with the actual component. (Default mode) <br>
 * <br>
 * If the mode is set to {@link #MODE_LAZY_LOAD_DRAW} the wrapper will wrap the
 * child component on the client side until it becomes visible and then render
 * it.
 * 
 */
public class VRefresherWrapper extends SimplePanel implements Container, Paintable {

	public static final String WRAPPER_AUTOREINIT_ON_REATTACH = "autoreinit";
	/*- Set the CSS class name to allow styling. */
	public static final String CLASSNAME = "v-lazyloadingwrapper";
	public static final String LOADING_CLASSNAME = "v-lazyloadingwrapper-loading";
	public static final String REFRESHING_CLASSNAME = "v-lazyloadingwrapper-refreshing";

	/* The ID's used in server communication */
	public static final String WIDGET_VISIBLE_ID = "widgetvisible";
	public static final String STATIC_CONTAINER = "staticcontainer";
	public static final String PLACEHOLDER_HEIGHT = "placeholderheight";
	public static final String PLACEHOLDER_WIDTH = "placeholderwidth";
	public static final String REFRESH_INTERVAL = "refreshinterval";

	public static final int MODE_LAZY_LOAD_FETCH = 1;
	public static final int MODE_LAZY_LOAD_DRAW = 2;

	/** The client side widget identifier */
	protected String wrappersPaintableId;

	/** Reference to the server connection object. */
	protected ApplicationConnection client;

	private Paintable lazyLoadPaintableComponent;
	private VCaptionWrapper captionWrapper;
	private UIDL childUIDL;

	private boolean staticContainer = false;

	/** Polling timer used to check for visibility */
	protected static LLWPoller visibilityPollingTimer;

	/** Timer used when visible delay is defined */
	private Timer visibleDelayTimer = null;

	private Element placeholder = null;
	private boolean recentlyAttached = false;
	private int refreshInterval;
	private Paintable p;

	@Override
	protected void onAttach() {
		super.onAttach();
		/*
		 * We take note that the wrapper has just been attached. The recently
		 * attached parameter will be set to false during the next update from
		 * the server, but this way we can catch the event where the wrapper is
		 * reattached and should automatically reinitialize itself...
		 */
		recentlyAttached = true;
	}

	/**
	 * Creates a new instance of the Lazy load wrapper (LLW) client side
	 * implementation, sets the style name and initiates the visibility polling
	 * timer if it's not initiated.
	 */
	public VRefresherWrapper() {
		super();

		/* Set the style name (spinner) to the placeholder */
		setStylePrimaryName(LOADING_CLASSNAME);
		placeholder = DOM.createDiv();
		getElement().appendChild(placeholder);

		createVisibleDelayTimer();
		visibleDelayTimer.schedule(500);
	}

	/**
	 * Creates the timer that is used when visibleDelay is defined.
	 */
	private void createVisibleDelayTimer() {
		visibleDelayTimer = new Timer() {
			@Override
			public void run() {
				update();
			}
		};

	}

	/**
	 * Called whenever an update is received from the server
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

		if (wrappersPaintableId == null) {
			wrappersPaintableId = client.getPid(this);
		}

		updateToThisLLW(uidl, client);
	}

	/**
	 * Process the update from the server.
	 * 
	 * @param uidl
	 * @param client
	 */
	private void updateToThisLLW(UIDL uidl, ApplicationConnection client) {

		// This call should be made first.
		// It handles sizes, captions, tooltips, etc. automatically.
		if (client.updateComponent(this, uidl, false)) {
			// If client.updateComponent returns true there has been no
			// changes and we
			// do not need to update anything.
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;

		if (checkForNeedOfAutomaticReinitOnReattach(uidl)) {
			return;
		}
		recentlyAttached = false;

		processVariableUpdatesFromServer(uidl);

		// VConsole.log("LLW:" + wrappersPaintableId + " uidl has "
		// + uidl.getChildCount()
		// + " children. Parent attachment status: " + isAttached());

		/*
		 * If UIDL has child we should paint it inside the placeholder or
		 * configure for MODE_LAZY_LOAD_DRAW
		 */
		if (uidl.getChildCount() > 0 && isAttached()) {
			drawChildFromUIDL(uidl, client);
			if (refreshInterval != 0) {
				if (visibleDelayTimer != null)
					visibleDelayTimer.schedule(refreshInterval);
			}
		}
	}

	private boolean checkForNeedOfAutomaticReinitOnReattach(UIDL uidl) {
		if (lazyLoadPaintableComponent == null && uidl.getBooleanAttribute(WIDGET_VISIBLE_ID) && recentlyAttached) {

			VConsole.error("Found that we should reinit the wrapper... ");
			recentlyAttached = false;
			if (uidl.hasAttribute(WRAPPER_AUTOREINIT_ON_REATTACH) && uidl.getBooleanAttribute(WRAPPER_AUTOREINIT_ON_REATTACH)) {
				client.updateVariable(wrappersPaintableId, WIDGET_VISIBLE_ID, false, true);
				return true;
			}

		}
		return false;
	}

	/**
	 * Process the variable updates from the server and set the local variables.
	 * 
	 * @param uidl
	 *            - the new UIDL instance
	 */
	private void processVariableUpdatesFromServer(UIDL uidl) {
		staticContainer = uidl.getBooleanAttribute(STATIC_CONTAINER);
		refreshInterval = uidl.getIntAttribute(REFRESH_INTERVAL);

		// Set the placeholder to size
		if (placeholder != null) {
			DOM.setStyleAttribute((com.google.gwt.user.client.Element) placeholder, "width", uidl.getStringAttribute(PLACEHOLDER_WIDTH));
			DOM.setStyleAttribute((com.google.gwt.user.client.Element) placeholder, "height", uidl.getStringAttribute(PLACEHOLDER_HEIGHT));
		}

	}

	/**
	 * Draw the child from UIDL
	 * 
	 * @param uidl
	 * @param client
	 */
	private void drawChildFromUIDL(UIDL uidl, ApplicationConnection client) {

		// Remove the placeholder
		if (placeholder != null) {
			getElement().removeChild(placeholder);
			placeholder = null;
			// remove the spinner and decos...
		}

		this.setStyleName(CLASSNAME);

		/* First child must first be attached to DOM, then updated */
		UIDL childUIDL = uidl.getChildUIDL(0);
		if (p != null)
			remove((Widget) p);
		p = client.getPaintable(uidl.getChildUIDL(0));
		add((Widget) p);

		// Tell the child to update itself from UIDL
		p.updateFromUIDL(childUIDL, client);
	}

	/**
	 * Called when we have determined that the wrapper is visible
	 */
	protected void update() {
		if (!isAttached()) {
			VConsole.log("The wrapper with PID: " + wrappersPaintableId + " is no longer attached to the DOM, ignoring paint of child component... ");
			return;
		}

		this.setStyleName(REFRESHING_CLASSNAME);
		client.updateVariable(wrappersPaintableId, WIDGET_VISIBLE_ID, true, true);
	}

	/*
	 * Container methods
	 */
	public RenderSpace getAllocatedSpace(Widget child) {

		if (staticContainer) {
			return new RenderSpace(getOffsetWidth(), getOffsetHeight());
		} else {

			RenderSpace llwRS = Util.getLayout(this).getAllocatedSpace(this);
			RenderSpace rs = new RenderSpace(llwRS.getWidth(), llwRS.getHeight());

			return rs;

		}

	}

	public boolean hasChildComponent(Widget component) {

		if (getWidget() == component) {
			return true;
		}

		return false;

	}

	public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
		setWidget(newComponent);
	}

	public boolean requestLayout(Set<Paintable> children) {
		if (staticContainer) {
			return true;
		}

		if (getElement().getStyle().getHeight().equalsIgnoreCase("") || getElement().getStyle().getWidth().equalsIgnoreCase("")) {
			return false;
		}

		return true;
	}

	public void updateCaption(Paintable component, UIDL uidl) {
		if (VCaption.isNeeded(uidl)) {
			if (captionWrapper != null) {
				captionWrapper.updateCaption(uidl);
			} else {
				captionWrapper = new VCaptionWrapper(component, client);
				setWidget(captionWrapper);
				captionWrapper.updateCaption(uidl);
			}
		} else {
			if (captionWrapper != null) {
				setWidget((Widget) lazyLoadPaintableComponent);
			}
		}
	}
}

/**
 * The static poller that's shared with all LLW:s in an application. When the
 * poller is triggered, all LLW instances will be called to check their
 * visibility.
 */
class LLWPoller extends Timer {

	ArrayList<VRefresherWrapper> listeners = new ArrayList<VRefresherWrapper>();

	@Override
	public void run() {
		VRefresherWrapper[] currListeners = new VRefresherWrapper[1];
		currListeners = listeners.toArray(currListeners);
		for (VRefresherWrapper llw : currListeners) {
			llw.update();
		}

	}

	/**
	 * Register a lazy load wrapper to the master poller
	 * 
	 * @param llw
	 *            - the LLW instance to be registered
	 */
	public synchronized void addLLW(VRefresherWrapper llw) {

		listeners.add(llw);
		if (listeners.size() == 1) {
			scheduleRepeating(250);
		}

	}

	/**
	 * Remove a llw from the master poller.
	 * 
	 * @param llw
	 *            - the instance of the llw to be removed.
	 */
	public synchronized void removeLLW(VRefresherWrapper llw) {
		listeners.remove(llw);
		if (listeners.isEmpty()) {
			cancel();
		}

	}
}