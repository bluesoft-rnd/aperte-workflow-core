package pl.net.bluesoft.casemanagement.model;

import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by Dominik Dębowczyk on 2015-08-13.
 */
@Entity
@Table(name = "pt_case_stage_files_attr", schema = CASES_SCHEMA)
public class FilesRepositoryCaseStageAttribute extends CaseStageAttribute implements IFilesRepositoryAttribute {
    public static final String TABLE = CASES_SCHEMA + "." + FilesRepositoryCaseStageAttribute.class.getAnnotation(Table.class).name();
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "case_stage_file_id")
    @Index(name = "idx_pt_files_case_stage_file_id")
    private Set<FilesRepositoryItem> filesRepositoryItems = new HashSet<FilesRepositoryItem>();

    @Override
    public Set<FilesRepositoryItem> getFilesRepositoryItems() {
        return filesRepositoryItems;
    }

    @Override
    public String getParentObjectPropertyName() {
        return getStage().getName();
    }

    @Override
    public Long getParentObjectId() {
        return getStage().getId();
    }

    @Override
    public void removeItem(FilesRepositoryItem item) {
        getFilesRepositoryItems().remove(item);
    }

    public void setFilesRepositoryItems(Set<FilesRepositoryItem> filesRepositoryItems) {
        this.filesRepositoryItems = filesRepositoryItems;
    }
}
