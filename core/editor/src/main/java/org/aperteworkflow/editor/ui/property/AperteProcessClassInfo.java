package org.aperteworkflow.editor.ui.property;

import org.apache.commons.lang.StringUtils;
import org.aperteworkflow.editor.stepeditor.user.Property;
import org.aperteworkflow.editor.ui.permission.PermissionDefinition;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.*;
import pl.net.bluesoft.rnd.util.AnnotationUtil;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Classes;

import java.lang.reflect.Field;
import java.util.*;

public class AperteProcessClassInfo {

	private List<Property<?>> properties;
	private List<PermissionDefinition> permissions; 
	private String docName;
	private String docDescription;
	private String docIcon;
	private String aliasName;
	private boolean childrenAllowed;
	private Class<?> aperteClass;
	
	public AperteProcessClassInfo() {}
	
	public AperteProcessClassInfo(Class<?> aperteClass) {
		this(aperteClass, null);
	}
	
	public AperteProcessClassInfo(Class<?> aperteClass, Set<Permission> defaultPermissions) {
		this.aperteClass = aperteClass;
		ChildrenAllowed ca = aperteClass.getAnnotation(ChildrenAllowed.class);
		AperteDoc classDoc = Classes.getClassAnnotation(aperteClass, AperteDoc.class);
		List<Field> fields = Classes.getFieldsWithAnnotation(aperteClass, AutoWiredProperty.class);
		PermissionsUsed permissionsUsed = Classes.getClassAnnotation(aperteClass, PermissionsUsed.class);

		aliasName = AnnotationUtil.getAliasName(aperteClass);
		childrenAllowed = ca == null ? false : ca.value();
        docName = null;
        docDescription = null;
        properties = new ArrayList<Property<?>>();
        permissions = getPermissionsFromAnnotation(permissionsUsed, defaultPermissions);
        
        I18NSource i18NSource = I18NSource.ThreadUtil.getThreadI18nSource();
        
		if (classDoc != null) {
			docName = i18NSource.getMessage(classDoc.humanNameKey());
			docDescription = i18NSource.getMessage(classDoc.descriptionKey());
			docIcon = i18NSource.getMessage(classDoc.icon());
		}
		if (StringUtils.isEmpty(docName)) {
			docName = aliasName;
		}
		if (StringUtils.isEmpty(docDescription)) {
			docDescription = aperteClass.getSimpleName();
		}
		
        if (fields != null && !fields.isEmpty()) {
        	for (Field field : fields) {
        		properties.add(getProperty(field));
        	}
        	Collections.sort(properties);
            
        }
	}
	
	private Property getProperty(Field field) {
		AutoWiredProperty awp = field.getAnnotation(AutoWiredProperty.class);
        AutoWiredPropertyConfigurator awpConfigurator = field.getAnnotation(AutoWiredPropertyConfigurator.class);
        AperteDoc fieldDoc = field.getAnnotation(AperteDoc.class);
        I18NSource i18NSource = I18NSource.ThreadUtil.getThreadI18nSource();
        
        String fieldDocName = null;
        String fieldDocDescription = null;
        
        if (fieldDoc != null) {
            fieldDocName = i18NSource.getMessage(fieldDoc.humanNameKey());
            fieldDocDescription = i18NSource.getMessage(fieldDoc.descriptionKey());
        }
        if (StringUtils.isEmpty(fieldDocName)) {
        	fieldDocName = field.getName();
        }
        if (StringUtils.isEmpty(fieldDocDescription)) {
        	fieldDocDescription = field.getName();
        }
        Property property = new Property(null, field.getType());
        property.setPropertyId(field.getName());
        property.setName(fieldDocName);
        property.setDescription(fieldDocDescription);
        
        if (awp != null) {
            property.setRequired(awp.required());
        }
        if (awpConfigurator != null) {
            property.setPropertyFieldClass(awpConfigurator.fieldClass());
        }
        return property;
	}

	public List<Property<?>> getProperties() {
		return properties;
	}

	public void setProperties(List<Property<?>> properties) {
		this.properties = properties;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getDocDescription() {
		return docDescription;
	}

	public void setDocDescription(String docDescription) {
		this.docDescription = docDescription;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public boolean isChildrenAllowed() {
		return childrenAllowed;
	}

	public void setChildrenAllowed(boolean childrenAllowed) {
		this.childrenAllowed = childrenAllowed;
	}

	public Class<?> getAperteClass() {
		return aperteClass;
	}

	public void setAperteClass(Class<?> aperteClass) {
		this.aperteClass = aperteClass;
	}

	public String getDocIcon() {
		return docIcon;
	}

	public void setDocIcon(String docIcon) {
		this.docIcon = docIcon;
	}

	public List<PermissionDefinition> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<PermissionDefinition> permissions) {
		this.permissions = permissions;
	}
	
	private List<PermissionDefinition> getPermissionsFromAnnotation(PermissionsUsed permissionsUsed, Set<Permission> defaultPermissions) {
        List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();

        Set<Permission> perms = null;
        if (permissionsUsed == null) {
            // only use default permissions when widget does not provide own annotation
            perms = defaultPermissions;
        } else {
            Permission[] usedPermissions = permissionsUsed.value();
            if (usedPermissions != null && usedPermissions.length > 0) {
                perms = new HashSet<Permission>();
                perms.addAll(Arrays.asList(usedPermissions));
            }
        }

        if (perms != null) {
            for (Permission perm : perms) {
                PermissionDefinition permissionDefinition = new PermissionDefinition();
                permissionDefinition.setKey(perm.key());
                permissionDefinition.setDescription(perm.desc());
                permissions.add(permissionDefinition);
            }

            Collections.sort(permissions);
        }

		return permissions;
	}
}
