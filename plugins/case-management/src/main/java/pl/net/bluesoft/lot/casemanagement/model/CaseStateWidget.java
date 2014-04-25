package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_widget")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_widget",
        indexes = {
                @Index(name = "idx_pt_case_state_widget_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateWidget extends PersistentEntity {
    private static final String PARENT_ID = "parent_id";
    static final String CASE_STATE_WIDGET_ID = "case_state_widget_id";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateDefinition.CASE_STATE_DEFINITION_ID)
    @Index(name = "idx_pt_case_state_wid_csd_id")
    private CaseStateDefinition caseStateDefinition;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = PARENT_ID)
    @Index(name = "idx_pt_case_state_widget_p_id")
    private CaseStateWidget parent;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = PARENT_ID)
    private Set<CaseStateWidget> children = new HashSet<CaseStateWidget>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_STATE_WIDGET_ID)
    private Set<CaseStateWidgetAttribute> attributes = new HashSet<CaseStateWidgetAttribute>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_STATE_WIDGET_ID)
    private Set<CaseStateWidgetPermission> permissions = new HashSet<CaseStateWidgetPermission>();

    public CaseStateWidget getParent() {
        return parent;
    }

    public void setParent(CaseStateWidget parent) {
        this.parent = parent;
    }

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Set<CaseStateWidgetAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<CaseStateWidgetAttribute> attributes) {
        this.attributes = attributes;
    }

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
}

