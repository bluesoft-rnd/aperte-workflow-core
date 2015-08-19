package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.config.IStateWidget;
import pl.net.bluesoft.rnd.util.CollectionComparer;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_widget", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_widget",
        indexes = {
                @Index(name = "idx_pt_case_state_widget_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateWidget extends PersistentEntity implements IStateWidget {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStateWidget.class.getAnnotation(Table.class).name();
    private static final String PARENT_ID = "parent_id";
    static final String CASE_STATE_WIDGET_ID = "case_state_widget_id";

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateDefinition.CASE_STATE_DEFINITION_ID)
    @Index(name = "idx_pt_case_state_wid_csd_id")
    private CaseStateDefinition caseStateDefinition;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = PARENT_ID)
    @Index(name = "idx_pt_case_state_widget_p_id")
    private CaseStateWidget parent;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = PARENT_ID)
    private Set<CaseStateWidget> children = new HashSet<CaseStateWidget>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CASE_STATE_WIDGET_ID)
    private Set<CaseStateWidgetAttribute> attributes = new HashSet<CaseStateWidgetAttribute>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CASE_STATE_WIDGET_ID)
    private Set<CaseStateWidgetPermission> permissions = new HashSet<CaseStateWidgetPermission>();

    public CaseStateWidget getParent() {
        return parent;
    }

    public void setParent(CaseStateWidget parent) {
        this.parent = parent;
    }

    @Override
	public Set<CaseStateWidget> getChildren() {
        return children;
    }

    public void setChildren(Set<CaseStateWidget> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
	public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
	public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
	public Set<CaseStateWidgetAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<CaseStateWidgetAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
	public Set<CaseStateWidgetPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<CaseStateWidgetPermission> permissions) {
        this.permissions = permissions;
    }

    public CaseStateDefinition getCaseStateDefinition() {
        return caseStateDefinition;
    }

    public void setCaseStateDefinition(CaseStateDefinition caseStateDefinition) {
        this.caseStateDefinition = caseStateDefinition;
    }

    @Override
    public CaseStateWidgetAttribute getAttributeByName(final String name) {
        for (CaseStateWidgetAttribute attribute : this.attributes)
            if (attribute.getName().equals(name))
                return attribute;
        return null;
    }

    private Object readResolve() {
        if (this.attributes == null)
            this.attributes = new HashSet<CaseStateWidgetAttribute>();
        if (this.permissions == null)
            this.permissions = new HashSet<CaseStateWidgetPermission>();
        return this;
    }

	public CaseStateWidget deepClone() {
		CaseStateWidget result = new CaseStateWidget();
		result.name = name;
		result.className = className;
		result.priority = priority;
		if (children != null) {
			for (CaseStateWidget child : children) {
				result.children.add(child.deepClone());
			}
		}
		if (attributes != null) {
			for (CaseStateWidgetAttribute attribute : attributes) {
				result.attributes.add(attribute.deepClone());
			}
		}
		if (permissions != null) {
			for (CaseStateWidgetPermission permission : permissions) {
				result.permissions.add(permission.deepClone());
			}
		}
		return result;
	}

	public boolean isSimilar(CaseStateWidget widget) {
		return Lang.equals(name, widget.name) &&
				Lang.equals(className, widget.className) &&
				Lang.equals(priority, widget.priority) &&
				CaseStateDefinition.WIDGET_COMPARER.compare(children, widget.children) &&
				ATTRIBUTE_COMPARER.compare(attributes, widget.attributes) &&
				PERMISSION_COMPARER.compare(permissions, widget.permissions);
	}

	private static final CollectionComparer<CaseStateWidgetAttribute> ATTRIBUTE_COMPARER = new CollectionComparer<CaseStateWidgetAttribute>() {
		@Override
		protected String getKey(CaseStateWidgetAttribute item) {
			return item.getKey();
		}

		@Override
		protected boolean compareItems(CaseStateWidgetAttribute item1, CaseStateWidgetAttribute item2) {
			return item1.isSimilar(item2);
		}
	};

	private static final CollectionComparer<CaseStateWidgetPermission> PERMISSION_COMPARER = new CollectionComparer<CaseStateWidgetPermission>() {
		@Override
		protected String getKey(CaseStateWidgetPermission item) {
			return item.getPrivilegeName();
		}

		@Override
		protected boolean compareItems(CaseStateWidgetPermission item1, CaseStateWidgetPermission item2) {
			return item1.isSimilar(item2);
		}
	};
}

