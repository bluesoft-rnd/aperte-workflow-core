package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.Application;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.processtool.i18n.DefaultI18NSource;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.StepEditorApplication;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.util.lang.Classes;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class WidgetInfoLoader {

	private static final class FieldTransformer implements Transformer {
		@Override
		public Object transform(Object o) {
			Field field = (Field) o;

			AutoWiredProperty awp = field.getAnnotation(AutoWiredProperty.class);
            AutoWiredPropertyConfigurator awpConfigurator = field.getAnnotation(AutoWiredPropertyConfigurator.class);

			Map<String, String> docMap = getDocumentation(o);
			if (!docMap.containsKey("name"))
				docMap.put("name", field.getName());
			if (!docMap.containsKey("description"))
				docMap.put("description", field.getName());

            Property property = new Property(null, field.getType());
            property.setPropertyType(Property.PropertyType.PROPERTY);
            property.setPropertyId(field.getName());
            property.setName(docMap.get("name"));
            property.setDescription(docMap.get("description"));
            
            if (awp != null) {
                property.setRequired(awp.required());
            }
            if (awpConfigurator != null) {
                property.setPropertyFieldClass(awpConfigurator.fieldClass());
            }

			return property;
		}
    }

	private static final class WidgetTransformer implements Transformer {
		private BundleItem	bundle;

		public WidgetTransformer(BundleItem bundleItem) {
			this.bundle = bundleItem;
		}

		@Override
		public Object transform(Object widgetClassObj) {
			final Class<?> widgetClass = (Class<?>) widgetClassObj;
			AliasName a = Classes.getClassAnnotation(widgetClass, AliasName.class);
			PermissionsUsed p = Classes.getClassAnnotation(widgetClass, PermissionsUsed.class);
			ChildrenAllowed ca = widgetClass.getAnnotation(ChildrenAllowed.class);
			boolean childrenAllowed = ca == null ? false : ca.value();

			Map<String, String> docMap = getDocumentation(widgetClass);
			if (!docMap.containsKey("name"))
				docMap.put("name", a.name());
			if (!docMap.containsKey("description"))
				docMap.put("description", widgetClass.getName());

			if (Boolean.valueOf(docMap.get("internal")))
				return null;

            List<Property<?>> properties = getProperties(widgetClass);
            List<Property<?>> permissions = getPermissions(p);

			return new WidgetItem(a.name(), docMap.get("name"), docMap.get("description"),
                    docMap.get("icon"), properties, permissions, childrenAllowed,
					bundle);
        }

        private List<Property<?>> getProperties(Class<?> widgetClass) {
            List<Property<?>> properties = new ArrayList<Property<?>>();
            
            List<Field> fields = Classes.getFieldsWithAnnotation(widgetClass, AutoWiredProperty.class);
            if (fields == null || fields.isEmpty()) {
                return properties;
            }
            
            properties.addAll(CollectionUtils.collect(fields, new FieldTransformer()));
            Collections.sort(properties);

            return properties;
        }
        
		private List<Property<?>> getPermissions(PermissionsUsed permissionsUsed) {
			List<Property<?>> permissions = new ArrayList<Property<?>>();
			if (permissionsUsed == null || permissionsUsed.value() == null) {
				return permissions;
            }

			for (Permission perm : permissionsUsed.value()) {
			    String permDesc = StringUtils.isEmpty(perm.desc()) ? PERMISSION_DESC_PREFIX + perm.key() : perm.desc();
			    permDesc = i18NSource.getMessage(permDesc, i18NProviders);
                
                Property property = new Property(null, String.class);
                property.setPropertyType(Property.PropertyType.PERMISSION);
                property.setPropertyId(perm.key());
                property.setName(permDesc + " (" + perm.key() + ")");
                property.setRequired(false);

			    permissions.add(property);
			}

            Collections.sort(permissions);

			return permissions;
		}
	}

    private static final String PERMISSION_DESC_PREFIX = "widget.permission.desc.";
    private static final String BUNDLE_DESC_PREFIX = "widget.bundle.desc.";

    private static DefaultI18NSource i18NSource = new DefaultI18NSource();
    private static Collection<I18NProvider>	i18NProviders;


	public static Map<BundleItem, Collection<WidgetItem>> loadAvailableWidgets(Application application)
            throws ClassNotFoundException {
		ProcessToolRegistry reg = StepEditorApplication.getRegistry();
		i18NSource.setLocale(application.getLocale());
		i18NProviders = reg.getI18NProviders();

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
        for (String bundleName : sortedWidgets.keySet()) {
            String bundleDescriptionKey = BUNDLE_DESC_PREFIX + bundleName;

            BundleItem bundleItem = new BundleItem(
                    bundleName,
                    i18NSource.getMessage(bundleDescriptionKey),
                    new ArrayList<I18NProvider>(),
                    new ArrayList<URL>()
            );

            Collection widgets = CollectionUtils.collect(sortedWidgets.get(bundleName), new WidgetTransformer(bundleItem));
            while (widgets.remove(null));
            if (widgets.size() > 0) {
                availableWidgets.put(bundleItem, widgets);
            }
        }

		return availableWidgets;
	}

	protected static Map<String, String> getDocumentation(Object object) {
		AperteDoc doc = null;
		if (object instanceof Class) {
			doc = Classes.getClassAnnotation((Class<?>) object, AperteDoc.class);
		} else if (object instanceof Field) {
			doc = ((Field) object).getAnnotation(AperteDoc.class);
		}

		Map<String, String> docMap = new HashMap<String, String>();
		if (doc != null) {
			docMap.put("name", i18NSource.getMessage(doc.humanNameKey(), i18NProviders));
			docMap.put("description", i18NSource.getMessage(doc.descriptionKey(), i18NProviders));
			docMap.put("icon", doc.icon());
			docMap.put("internal", doc.internal() ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
		} else {
			docMap.put("internal", Boolean.FALSE.toString());
		}
		return docMap;
	}

}
