package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_stage", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_stage",
        indexes = {
                @Index(name = "idx_pt_case_stage_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStage extends PersistentEntity implements IAttributesProvider, IAttributesConsumer {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStage.class.getAnnotation(Table.class).name();
    final static String CASE_STAGE_ID = "case_stage_id";

    @Column(name = "name")
    private String name;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Case.CASE_ID)
    @Index(name = "idx_pt_case_stage_case_id")
    private Case caseInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_definition_id")
    private CaseStateDefinition caseStateDefinition;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CASE_STAGE_ID)
    private Set<CaseStageSimpleAttribute> simpleAttributes = new HashSet<CaseStageSimpleAttribute>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = CASE_STAGE_ID)
	private Set<CaseStageSimpleLargeAttribute> simpleLargeAttributes = new HashSet<CaseStageSimpleLargeAttribute>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = CASE_STAGE_ID)
	private Set<CaseStageAttribute> attributes = new HashSet<CaseStageAttribute>();

    public void setSimpleAttributes(Set<CaseStageSimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

    public CaseStateDefinition getCaseStateDefinition() {
        return caseStateDefinition;
    }

    public void setCaseStateDefinition(CaseStateDefinition caseStateDefinition) {
        this.caseStateDefinition = caseStateDefinition;
    }

    public Case getCase() {
        return caseInstance;
    }

    public void setCase(Case caseInstance) {
        this.caseInstance = caseInstance;
    }

    public Set<CaseStageSimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

	public Set<CaseStageSimpleLargeAttribute> getSimpleLargeAttributes() {
		return simpleLargeAttributes;
	}

	public void setSimpleLargeAttributes(Set<CaseStageSimpleLargeAttribute> simpleLargeAttributes) {
		this.simpleLargeAttributes = simpleLargeAttributes;
	}

	public Set<CaseStageAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<CaseStageAttribute> attributes) {
		this.attributes = attributes;
	}

	public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private CaseStageSimpleAttribute findSimpleAttributeByKey(String key) {
        Set<CaseStageSimpleAttribute> attrs = getSimpleAttributes();
        for (CaseStageSimpleAttribute cssa : attrs) {
            if (cssa.getKey() != null && cssa.getKey().equals(key)) {
                return cssa;
            }
        }
        return null;
    }

    public void setSimpleAttribute(final String key, final String value) {
        CaseStageSimpleAttribute attr = findSimpleAttributeByKey(key);
        if (attr != null) {
            attr.setValue(value);
        } else {
            attr = new CaseStageSimpleAttribute();
            attr.setKey(key);
            attr.setValue(value);
            attr.setStage(this);
            simpleAttributes.add(attr);
        }
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return null;
    }

    public String getSimpleAttributeValue(String key) {
        CaseStageSimpleAttribute attr = findSimpleAttributeByKey(key);
        if (attr != null)
            return attr.getValue();
        return null;
    }

	private CaseStageSimpleLargeAttribute findSimpleLargeAttributeByKey(String key) {
		Set<CaseStageSimpleLargeAttribute> attrs = getSimpleLargeAttributes();
		for (CaseStageSimpleLargeAttribute cssa : attrs) {
			if (cssa.getKey() != null && cssa.getKey().equals(key)) {
				return cssa;
			}
		}
		return null;
	}

	public void setSimpleLargeAttribute(String key, String value) {
		CaseStageSimpleLargeAttribute attr = findSimpleLargeAttributeByKey(key);
		if (attr != null) {
			attr.setValue(value);
		} else {
			attr = new CaseStageSimpleLargeAttribute();
			attr.setKey(key);
			attr.setValue(value);
			attr.setStage(this);
			simpleLargeAttributes.add(attr);
		}
	}

    @Override
    public void addAttribute(Object attribute) {
        addComplexAttribute((CaseStageAttribute) attribute);
    }

    @Override
    public void setAttribute(String key, Object attribute) {
        CaseStageAttribute caseStageAttribute = (CaseStageAttribute) attribute;
        caseStageAttribute.setKey(key);
        addComplexAttribute((CaseStageAttribute) attribute);
    }

    public String getSimpleLargeAttributeValue(String key) {
		CaseStageSimpleLargeAttribute attr = findSimpleLargeAttributeByKey(key);
		if (attr != null)
			return attr.getValue();
		return null;
	}

    @Override
    public String getExternalKey() {
        return getProcessInstance().getExternalKey();
    }

    @Override
    public String getDefinitionName() {
        return getCaseStateDefinition().getName();
    }

    @Override
    public Object getAttribute(String key) {
        return getComplexAttribute(key);
    }

    @Override
    public Object getProvider() {
        return this;
    }

    public CaseStageAttribute getComplexAttribute(String key) {
		for (CaseStageAttribute attribute : attributes) {
			if (attribute.getKey().equals(key)) {
				return attribute;
			}
		}
		return null;
	}

	public void addComplexAttribute(CaseStageAttribute attribute) {
		attributes.add(attribute);
		attribute.setStage(this);
	}
}
