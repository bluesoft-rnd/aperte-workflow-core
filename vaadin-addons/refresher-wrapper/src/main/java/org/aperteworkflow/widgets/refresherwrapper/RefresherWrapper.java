package org.aperteworkflow.widgets.refresherwrapper;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.aperteworkflow.widgets.refresherwrapper.widgetset.client.ui.VRefresherWrapper;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Component;

/**
 * Based on LazyLoadWrapper by Petri Heinonen 
 * 
 * https://vaadin.com/directory/#addon/lazyloadwrapper
 * 
 * 
 * 
 * Server side component for the VLazyLoadingWrapper widget.
 * 
 * A wrapper for loading Vaadin components lazily. The wrapper creates a
 * lightweight placeholder on the client side that has a spinner on it until the
 * user has scrolled the placeholder in to view at which point the wrapper will
 * draw the lazy load component. <br />
 * <br />
 * 
 * The component that is to be lazily loaded can be provided to the Lazy load
 * wrapper (LLW), through:
 * <ul>
 * <li>one of the constructors</li>
 * <li>the {@link RefresherWrapper#setLazyLoadComponent(Component)} -method</li>
 * <li>or the {@link LazyLoadComponentProvider#onComponentVisible()} -interface.
 * </li>
 * </ul>
 * If the lazy load component is has a defined width and/or height, the wrapper
 * will try to set the size of the placeholder to the size of the child
 * component automatically. If no sizing information is available, the wrapper
 * will set the (undefined size) for placeholder to a default value of of 100px <br />
 * <br />
 * The LLW has a proximity parameter that can be set through
 * {@link #setProximity(int)}. This works as a "fine tune" for the loading
 * event. The proximity is the offset from the visible area when the lazy load
 * component should be loaded from the server.<br />
 * * Positive numbers = before actually visible <br />
 * * Negative numbers = must be <i>X<i/> px visible <br />
 * * <b>Default: </b> 250px <br />
 * <br />
 * 
 * LLW can also be set to use a delay timer that defines a delay how long the
 * placeholder should be visible before the child component is drawn. <br>
 * * <b>Default: </b> 0 ms. <br>
 * <br>
 */

@SuppressWarnings("serial")
@ClientWidget(VRefresherWrapper.class)
public class RefresherWrapper extends AbstractComponentContainer {

	private Component lazyloadComponent = null;
	private boolean autoReinitLazyLoad = false;

	/**
	 * Defines if the container of the lazy load wrapper should be static or
	 * not. If false, the container will expand to fit the child component,
	 * while if true, the container will keep it's size and force the child to
	 * be drawn within the size defined by the placeholder.
	 */
	private boolean staticContainer = false;
	/**
	 * The instance of {@link LazyLoadComponentProvider} that will provide the
	 * child component when it's needed (server side lazy load).
	 */
	private LazyLoadComponentProvider childProvider = null;

	private String placeholderHeight = "100px";
	private String placeholderWidth = "100px";

	private Integer refreshIntervalMs = 5000;

	/**
	 * Create new Lazy load wrapper with default settings and no component.
	 */
	public RefresherWrapper() {
		super();

	}

	/* CONSTRUCTORS FOR SERVER SIDE LAZY LOAD */

	/**
	 * Create a lazy load wrapper with default settings and server side lazy
	 * load.
	 * 
	 * @param childProvider
	 *            - the instance of {@link LazyLoadComponentProvider} that will
	 *            provide the <i>component</i> when it's needed.
	 */
	public RefresherWrapper(LazyLoadComponentProvider childProvider) {
		super();
		this.childProvider = childProvider;
	}

	/**
	 * Create a lazy load wrapper with a defined
	 * {@link RefresherWrapper#proximity} and server side lazy load.
	 * 
	 * @param proximity
	 *            - the proximity in pixels from the viewable area when the
	 *            component should be loaded
	 * @param childProvider
	 *            - the instance of {@link LazyLoadComponentProvider} that will
	 *            provide the <i>component</i> when it's needed.
	 */
	public RefresherWrapper(int refreshIntervalMs, LazyLoadComponentProvider childProvider) {

		this(childProvider);
		this.refreshIntervalMs = refreshIntervalMs;
	}

	/**
	 * Create a new lazy load wrapper with server side lazy load and with a
	 * specified placeholder size that resizes itself to fit the child
	 * components when they are loaded.
	 * 
	 * @param placeHolderWidth
	 *            - the width of the placeholder
	 * @param placeHolderHeight
	 *            - the height of the placeholder
	 * @param childProvider
	 *            - the instance of {@link LazyLoadComponentProvider} that will
	 *            provide the <i>component</i> when it's needed.
	 */
	public RefresherWrapper(String placeHolderWidth, String placeHolderHeight, int refreshIntervalMs, LazyLoadComponentProvider childProvider) {

		this(childProvider);
		setPlaceHolderSize(placeHolderWidth, placeHolderHeight);
	}

	/**
	 * Create a new lazy load wrapper with server side lazy load and with a
	 * specified placeholder size that does/does not resize itself when the
	 * child components are loaded.
	 * 
	 * @param placeHolderWidth
	 *            - the width of the placeholder
	 * @param placeHolderHeight
	 *            - the height of the placeholder
	 * @param staticContainer
	 *            - true if the placeholder should keep it size after that the
	 *            components are loaded <br />
	 *            false if the placeholder should auto resize to accommodate
	 *            is's children.
	 * @param childProvider
	 *            - the instance of {@link LazyLoadComponentProvider} that will
	 *            provide the <i>component</i> when it's needed.
	 */
	public RefresherWrapper(String placeHolderWidth, String placeHolderHeight, boolean staticContainer, int refreshIntervalMs,
			LazyLoadComponentProvider childProvider) {

		this(placeHolderWidth, placeHolderHeight, refreshIntervalMs, childProvider);
		setStaticConatiner(staticContainer);
	}

	/*
	 * Methods
	 */

	/**
	 * @deprecated Use {@link #setLazyLoadComponent(Component)} instead.
	 */
	@Override
	@Deprecated
	public void addComponent(Component component) {
		throw new UnsupportedOperationException();
	}

	/*
	 * 
	 * Getters and setters
	 */

	/**
	 * Set the size of the place holder that will be shown on the client-side.
	 * 
	 * @param width
	 * @param height
	 */
	public void setPlaceHolderSize(String width, String height) {
		placeholderHeight = height;
		placeholderWidth = width;

		if (staticContainer) {
			setWidth(width);
			setHeight(height);
		} else {
			setSizeUndefined();
		}

		requestRepaint();

	}

	/**
	 * Sets the static container. If static container is set to true, the
	 * container will keep it's size when the lazy load component is loaded. <br/>
	 * <br/>
	 * If static container is set to false, the wrapper will try to
	 * expand/shrink to fit it's child component.
	 * 
	 * @param staticContainer
	 */
	public void setStaticConatiner(boolean staticContainer) {
		this.staticContainer = staticContainer;
		if (staticContainer) {
			setWidth(placeholderWidth);
			setHeight(placeholderHeight);
		} else {
			setSizeUndefined();
		}
		requestRepaint();
	}

	/**
	 * Get the current container mode.
	 */
	public boolean getStaticContainer() {
		return staticContainer;
	}

	/**
	 * Set the isVisible parameter for the wrapper. When called with <i>true</i>
	 * this will show the wrapped component immediately on the client side. <br>
	 * <br>
	 * <i> Note: not the same thing as Vaadins isVisible()</i><br>
	 * 
	 * 
	 * @param visible
	 * <br>
	 *            - true: show wrapped component immediately <br>
	 *            - false: show child component when scrolled into view.
	 */
	public void reloadComponent() {
		if (childProvider != null) {
			lazyloadComponent = childProvider.onComponentVisible();
		}

		// Attach child to container.
		if (lazyloadComponent != null) {
			super.addComponent(lazyloadComponent);
		}
		requestRepaint();
	}

	/*
	 * Server to Client communication
	 */

	@Override
	public void paintContent(PaintTarget target) throws PaintException {
		super.paintContent(target);

		target.addAttribute(VRefresherWrapper.PLACEHOLDER_HEIGHT, placeholderHeight);
		target.addAttribute(VRefresherWrapper.PLACEHOLDER_WIDTH, placeholderWidth);
		target.addAttribute(VRefresherWrapper.STATIC_CONTAINER, staticContainer);
		target.addAttribute(VRefresherWrapper.WRAPPER_AUTOREINIT_ON_REATTACH, autoReinitLazyLoad);
		target.addAttribute(VRefresherWrapper.REFRESH_INTERVAL, refreshIntervalMs);

		if (lazyloadComponent != null) {

			lazyloadComponent.paint(target);

		}

	}

	/**
	 * Receive and handle events and other variable changes from the client.
	 * 
	 * {@inheritDoc}
	 */

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (variables.containsKey(VRefresherWrapper.WIDGET_VISIBLE_ID)) {
			reloadComponent();
		}

	}

	public Iterator<Component> getComponentIterator() {
		Iterator<Component> iterator = new Iterator<Component>() {

			private boolean first = lazyloadComponent == null;

			public boolean hasNext() {

				return !first;
			}

			public Component next() {
				if (!first) {
					first = true;
					return lazyloadComponent;
				} else {
					return null;
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();

			}

		};

		return iterator;
	}

	public void replaceComponent(Component oldComponent, Component newComponent) {
		throw new UnsupportedOperationException();

	}

	/*
	 * 
	 * Server side lazy load...
	 */

	public void setAutoReinitLazyLoad(boolean autoReinitLazyLoad) {
		this.autoReinitLazyLoad = autoReinitLazyLoad;
	}

	public boolean isAutoReinitLazyLoad() {
		return autoReinitLazyLoad;
	}

	/**
	 * The listener interface for implementing server side lazy load. If no
	 * child component is specified for the lazy load wrapper, the wrapper will
	 * try to retrieve one using this interface when the placeholder on the
	 * client side is visible.
	 * 
	 */
	public interface LazyLoadComponentProvider extends Serializable {

		/**
		 * Called when the placeholder component has become visible
		 */
		public Component onComponentVisible();

	}

	/**
	 * Get the delay that the placeholder is visible on client side before the
	 * lazy load component is actually loaded.
	 * 
	 * @return placeholderVisibleDelay - the delay in ms
	 */
	public int getRefreshIntervalMs() {
		return refreshIntervalMs;
	}

	/**
	 * Set the delay how long the placeholder should be visible before the
	 * component is loaded from the server to the client.
	 * 
	 * @param placeholderVisibleDelay
	 *            - the delay in ms
	 */
	public void setRefreshIntervalMs(int refreshIntervalMs) {
		this.refreshIntervalMs = refreshIntervalMs;
		requestRepaint();
	}

}
