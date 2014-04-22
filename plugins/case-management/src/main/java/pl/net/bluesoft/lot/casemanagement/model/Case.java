package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case",
        indexes = {
                @Index(name = "idx_pt_case_pk",
                        columnNames = {"id"}
                )
        })
public class Case extends PersistentEntity {
    static final String CASE_ID = "case_id";

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number", nullable = false)
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_definition_id")
    private CaseDefinition definition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_" + CaseStage.CASE_STAGE_ID)
    private CaseStage currentStage;

    @Column(name = "creation_date", nullable = false)
    private Date createDate;

    @Column(name = "modification_date", nullable = true)
    private Date modificationDate;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_ID)
    private Set<CaseSimpleAttribute> simpleAttributes = new HashSet<CaseSimpleAttribute>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_ID)
    private Set<CaseAttribute> attributes = new HashSet<CaseAttribute>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<CaseStage> stages = new HashSet<CaseStage>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "pt_process_instance_case",
            joinColumns = {@JoinColumn(name = CASE_ID)},
            inverseJoinColumns = {@JoinColumn(name = "process_instance_id")}
    )
    private Set<ProcessInstance> processInstances;

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
}
