package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.*;

import static pl.net.bluesoft.casemanagement.model.Constants.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case",
        indexes = {
                @Index(name = "idx_pt_case_pk",
                        columnNames = {"id"}
                )
        })
@SqlResultSetMapping(name = "cnt", columns = @ColumnResult(name = "cnt"))
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "selectCase",
                query = "select * from select_complaint_case(:caseNumber, :caseShortNumber, :in_orderBy, :ascOrder, :pageSize, :currentPage, :createDate, :createDateTo, :createDateRange, :stages, :textSearch)",
                resultClass = Case.class
        ),
        @NamedNativeQuery(
                name = "selectCaseCount",
                query = "select select_complaint_case_count(:caseNumber, :caseShortNumber, :createDate, :createDateTo, :createDateRange, :stages, :textSearch) as cnt",
                resultSetMapping = "cnt"
        )
})
public class Case extends PersistentEntity implements IAttributesProvider, IAttributesConsumer {
    public static final String TABLE = CASES_SCHEMA + "." + Case.class.getAnnotation(Table.class).name();
    static final String CASE_ID = "case_id";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number", nullable = false)
    @Index(name = "idx_pt_case_number")
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_definition_id")
    private CaseDefinition definition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "current_" + CaseStage.CASE_STAGE_ID)
    @Index(name = "idx_pt_case_current_case_id")
    private CaseStage currentStage;

    @Column(name = "creation_date", nullable = false)
    @Index(name = "idx_pt_case_cdate")
    private Date createDate;

    @Column(name = "modification_date", nullable = true)
    private Date modificationDate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_ID)
    private Set<CaseSimpleAttribute> simpleAttributes = new HashSet<CaseSimpleAttribute>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_ID)
    private Set<CaseSimpleLargeAttribute> simpleLargeAttributes = new HashSet<CaseSimpleLargeAttribute>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_ID)
    private Set<CaseAttribute> attributes = new HashSet<CaseAttribute>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CASE_ID)
    private Set<CaseStage> stages = new HashSet<CaseStage>();

    @Transient
    @Column(name="total_count")
    private Long totalCount;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "pt_process_instance_case",
            joinColumns = {@JoinColumn(name = CASE_ID)},
            inverseJoinColumns = {@JoinColumn(name = "process_instance_id")}
    )
    private Set<ProcessInstance> processInstances;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private List<CaseLog> caseLog;

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CaseDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(CaseDefinition definition) {
        this.definition = definition;
    }

    public CaseStage getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(CaseStage currentStage) {
        this.currentStage = currentStage;
    }

    public Set<CaseSimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

    public void setSimpleAttributes(Set<CaseSimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

    public Set<CaseAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<CaseAttribute> attributes) {
        this.attributes = attributes;
    }

    public Set<ProcessInstance> getProcessInstances() {
        return processInstances;
    }

    public void setProcessInstances(Set<ProcessInstance> processInstances) {
        this.processInstances = processInstances;
    }

    public Set<CaseStage> getStages() {
        return stages;
    }

    public void setStages(Set<CaseStage> stages) {
        this.stages = stages;
    }

	public Collection<AbstractCaseAttribute> getAllAttributes() {
        Set<AbstractCaseAttribute> all = new HashSet<AbstractCaseAttribute>();
        all.addAll(this.getSimpleAttributes());
        all.addAll(this.getAttributes());
        return all;
    }

    public String getDefinitionName() {
        return this.getDefinition().getName();
    }

    public void setDefinitionName(final String definitionName) {
        this.getDefinition().setName(definitionName);
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return null;
    }

    @Override
    public String getSimpleAttributeValue(String key) {
        final CaseSimpleAttribute attr = findSimpleAttributeByKey(key);
        return attr != null ? attr.getValue() : null;
    }

    @Override
    public String getSimpleLargeAttributeValue(String key) {
        final CaseSimpleLargeAttribute attr = findSimpleLargeAttributeByKey(key);
        return attr != null ? attr.getValue() : null;
    }

    private CaseSimpleLargeAttribute findSimpleLargeAttributeByKey(String key) {
        Set<CaseSimpleLargeAttribute> attrs = getSimpleLargeAttributes();
        for (CaseSimpleLargeAttribute csa : attrs) {
            if (csa.getKey() != null && csa.getKey().equals(key)) {
                return csa;
            }
        }
        return null;
    }

    @Override
    public String getExternalKey() {
        return getNumber();
    }

    private CaseSimpleAttribute findSimpleAttributeByKey(String key) {
        Set<CaseSimpleAttribute> attrs = getSimpleAttributes();
        for (CaseSimpleAttribute csa : attrs) {
            if (csa.getKey() != null && csa.getKey().equals(key)) {
                return csa;
            }
        }
        return null;
    }

    @Override
    public void setSimpleAttribute(final String key, final String value) {
        CaseSimpleAttribute attr = findSimpleAttributeByKey(key);
        if (attr != null) {
            attr.setValue(value);
        } else {
            attr = new CaseSimpleAttribute();
            attr.setKey(key);
            attr.setValue(value);
            attr.setCase(this);
            simpleAttributes.add(attr);
        }
    }

    public void addAttribute(final CaseAttribute attribute) {
        attribute.setCase(this);
        getAttributes().add(attribute);
    }

    private void setAttributeByKey(final String key, final CaseAttribute attribute) {
        CaseAttribute attr = findAttributeByKey(key);
        if (attr != null) {
            getAttributes().remove(attr);
        }
        if (attribute != null) {
            attribute.setKey(key);
        }
        addAttribute(attribute);
    }

    @Override
    public void addAttribute(final Object attribute) {
        addAttribute((CaseAttribute) attribute);
    }

    @Override
    public void setAttribute(final String key, final Object attribute) {
        setAttributeByKey(key, (CaseAttribute) attribute);
    }

    @Override
    public Object getAttribute(String key) {
        return findAttributeByKey(key);
    }

    @Override
    public Object getProvider() {
        return this;
    }

    private CaseAttribute findAttributeByKey(String key) {
        Set<CaseAttribute> attrs = getAttributes();
        for (CaseAttribute ca : attrs) {
            if (ca.getKey() != null && ca.getKey().equals(key)) {
                return ca;
            }
        }
        return null;
    }

    public Set<CaseSimpleLargeAttribute> getSimpleLargeAttributes() {
        return simpleLargeAttributes;
    }

    public void setSimpleLargeAttributes(Set<CaseSimpleLargeAttribute> simpleLargeAttributes) {
        this.simpleLargeAttributes = simpleLargeAttributes;
    }

    public void setSimpleLargeAttribute(final String key, final String value) {
        CaseSimpleLargeAttribute attr = findSimpleLargeAttributeByKey(key);
        if (attr != null) {
            attr.setValue(value);
        } else {
            attr = new CaseSimpleLargeAttribute();
            attr.setKey(key);
            attr.setValue(value);
            attr.setCase(this);
            getSimpleLargeAttributes().add(attr);
        }
    }

    public List<CaseLog> getCaseLog() {
        return caseLog;
    }

    public void setCaseLog(List<CaseLog> caseLog) {
        this.caseLog = caseLog;
    }

    public List<CaseLog> getLogsSortedByDate() {
        List<CaseLog> list = new ArrayList<CaseLog>(caseLog);
        Collections.sort(list, new Comparator<CaseLog>() {
            @Override
            public int compare(CaseLog o1, CaseLog o2) {
                return o1.getEntryDate().compareTo(o2.getEntryDate());
            }
        });
        return list;
    }

    public CaseStage findCaseStageById(Long id) {
        if (id == null) {
			return null;
		}
		for (CaseStage stage : stages) {
            if (id.equals(stage.getId())) {
				return stage;
			}
        }
        return null;
    }

	public void removeStage(CaseStage stage) {
		stages.remove(stage);
		stage.setCase(null);
	}
}
