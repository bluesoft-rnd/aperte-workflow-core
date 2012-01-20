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
		public Object transform(Object arg0) {
			Field field = (Field) arg0;

			AutoWiredProperty awp = field.getAnnotation(AutoWiredProperty.class);

			Map<String, String> docMap = getDocumentation(arg0);
			if (!docMap.containsKey("name"))
				docMap.put("name", field.getName());
			if (!docMap.containsKey("description"))
				docMap.put("description", field.getName());

			return new Property(Property.PropertyType.PROPERTY, field.getName(), docMap.get("name"), docMap.get("description"), field.getType(), null, awp.required(), null);
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
			
			CustomConfigurator cc = widgetClass.getAnnotation(CustomConfigurator.class);
			Class<? extends WidgetConfigFormFieldFactory> configurator = cc == null ? null : cc.value();

			ChildrenAllowed ca = widgetClass.getAnnotation(ChildrenAllowed.class);
			boolean childrenAllowed = ca == null ? false : ca.value();

			Map<String, String> docMap = getDocumentation(widgetClass);
			if (!docMap.containsKey("name"))
				docMap.put("name", a.name());
			if (!docMap.containsKey("description"))
				docMap.put("description", widgetClass.getName());

			if (Boolean.valueOf(docMap.get("internal")))
				return null;

			List<Field> fields = Classes.getFieldsWithAnnotation(widgetClass, AutoWiredProperty.class);
			Collection<Property<?>> properties = CollectionUtils.collect(fields, new FieldTransformer());
			Collection<Property<?>> permissions = getPropertiesList(p);

			return new WidgetItem(a.name(), docMap.get("name"), docMap.get("description"),
                    docMap.get("icon"), properties, permissions, childrenAllowed, configurator,
					bundle);
		}
		
		private Collection<Property<?>> getPropertiesList(PermissionsUsed p) {
			Collection<Property<?>> permissions = new ArrayList<Property<?>>();
			
			if (p == null || p.value() == null)
				return permissions;
			
			for (Permission perm : p.value()) {
			  String permDesc = StringUtils.isEmpty(perm.desc()) ? PERMISSION_DESC_PREFIX + perm.key() : perm.desc();
			  permDesc = i18NSource.getMessage(permDesc, i18NProviders);
			  permissions.add(new Property(Property.PropertyType.PERMISSION, perm.key(), permDesc + " (" + perm.key() + ")", null, String.class, null, false, null));
			}
			return permissions;
		}
	}

    private static final String PERMISSION_DESC_PREFIX = "widget.permission.desc.";
    private static final String BUNDLE_DESC_PREFIX = "widget.bundle.desc.";

    private static DefaultI18NSource i18NSource = new DefaultI18NSource();
    private static Collection<I18NProvider>	i18NProviders;


	public static Map<BundleItem, Collection<WidgetItem>> loadAvailableWidgets(Application application)
            throws ClassNotFoundException {
		ProcessToolRegistry reg = StepEditorApplication.getRegistry(application);

		i18NSource.setLocale(application.getLocale());
		i18NProviders = reg.getI18NProviders();

		Map<BundleItem, Collection<WidgetItem>> availableWidgets = new HashMap<BundleItem, Collection<WidgetItem>>();

        Map<String, Class<? extends ProcessToolWidget>> registeredWidgets = reg.getAvailableWidgets();
        if (registeredWidgets == null || registeredWidgets.size() == 0) {
            return  availableWidgets;
        }

        // Create sorted structure of widgets by processing their annotations
        Map<String, List<Class<? extends ProcessToolWidget>>> sortedWidgets = new HashMap<String, List<Class<? extends ProcessToolWidget>>>();
        for (Class<? extends ProcessToolWidget> widgetClass : registeredWidgets.values()) {
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
