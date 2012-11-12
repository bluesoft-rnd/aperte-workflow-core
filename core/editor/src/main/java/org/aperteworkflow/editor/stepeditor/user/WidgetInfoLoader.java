package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.Application;
import org.aperteworkflow.editor.vaadin.GenericEditorApplication;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.Permission;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PermissionsUsed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class WidgetInfoLoader {
    

    private static final String BUNDLE_DESC_PREFIX;
    private static Set<Permission> DEFAULT_PERMISSIONS;

    static {

        BUNDLE_DESC_PREFIX = "widget.bundle.desc.";

        DEFAULT_PERMISSIONS = new HashSet<Permission>();
        Permission[] dp = (Permission[]) getDefaultValue(PermissionsUsed.class);
        if (dp != null) {
            DEFAULT_PERMISSIONS.addAll(Arrays.asList(dp));
        }
    }
    
    // TODO consider moving to Classes in bluesoft-util
    private static Object getDefaultValue(Class<? extends Annotation> annotationType) {
        return getDefaultValue(annotationType, "value");
    }

    private static Object getDefaultValue(Class<? extends Annotation> annotationType, String attributeName) {
        try {
            Method method = annotationType.getDeclaredMethod(attributeName, new Class[0]);
            return method.getDefaultValue();
        } catch (Exception ex) {
            return null;
        }
    }

	public static Map<BundleItem, Collection<WidgetItem>> loadAvailableWidgets(Application application)
            throws ClassNotFoundException {
		ProcessToolRegistry reg = GenericEditorApplication.getRegistry();
        I18NSource i18NSource = I18NSource.ThreadUtil.getThreadI18nSource();

		Map<BundleItem, Collection<WidgetItem>> availableWidgets = new HashMap<BundleItem, Collection<WidgetItem>>();

        Map<String, Class<? extends ProcessToolWidget>> registeredWidgets = reg.getAvailableWidgets();
        if (registeredWidgets == null || registeredWidgets.size() == 0) {
            return  availableWidgets;
        }

        // Widgets are available in registry by both @AliasName and their class name
        // we don't want to present each step twice so we exclude duplicates. Do note
        // that java.lang.Class does not override hashCode() and equals() so we use it's name
        Map<String, Class<? extends ProcessToolWidget>> viewableWidgets = new HashMap<String, Class<? extends ProcessToolWidget>>();
        for (Class<? extends ProcessToolWidget> widgetClass : registeredWidgets.values()) {
            viewableWidgets.put(widgetClass.getName(), widgetClass);
        }

        // Create sorted structure of widgets by processing their annotations
        Map<String, List<Class<? extends ProcessToolWidget>>> sortedWidgets = new HashMap<String, List<Class<? extends ProcessToolWidget>>>();
        for (Class<? extends ProcessToolWidget> widgetClass : viewableWidgets.values()) {
            String widgetGroupName = "unsorted";
            
            WidgetGroup widgetGroup = Classes.getClassAnnotation(widgetClass, WidgetGroup.class);
            if (widgetGroup != null) {
                widgetGroupName = widgetGroup.value();
            }

            List<Class<? extends ProcessToolWidget>> widgetGroupItems = sortedWidgets.get(widgetGroupName);
            if (widgetGroupItems == null) {
                widgetGroupItems = new ArrayList<Class<? extends ProcessToolWidget>>();
                sortedWidgets.put(widgetGroupName, widgetGroupItems);
            }

            widgetGroupItems.add(widgetClass);
        }

        // Process the sorted structure to final form
		for (Map.Entry<String, List<Class<? extends ProcessToolWidget>>> e : sortedWidgets.entrySet()) {
			String bundleName = e.getKey();
            String bundleDescriptionKey = BUNDLE_DESC_PREFIX + bundleName;

            BundleItem bundleItem = new BundleItem(
                    bundleName,
                    i18NSource.getMessage(bundleDescriptionKey),
                    new ArrayList<I18NProvider>(),
                    new ArrayList<URL>()
            );

            
            Collection<WidgetItem> widgets = new ArrayList<WidgetItem>();
            for (Class widgetClass : e.getValue()) {
            	 widgets.add(new WidgetItem(widgetClass,DEFAULT_PERMISSIONS,bundleItem));
            }

			removeNulls(widgets);

			if (widgets.size() > 0) {
                availableWidgets.put(bundleItem, widgets);
            }
        }

		return availableWidgets;
	}

	private static void removeNulls(Collection<WidgetItem> widgets) {
		for (;;) {
			if (!widgets.remove(null)) {
				return;
			}
		}
	}

}
