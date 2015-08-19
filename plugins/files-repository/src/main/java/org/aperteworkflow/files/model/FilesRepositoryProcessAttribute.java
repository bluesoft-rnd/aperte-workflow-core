package org.aperteworkflow.files.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceAttribute;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pkuciapski on 2014-05-12.
 */
@Entity
@Table(name = "pt_process_instance_files_attr")
public class FilesRepositoryProcessAttribute extends ProcessInstanceAttribute implements IFilesRepositoryAttribute {
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "file_id")
    @Index(name = "idx_pt_files_prc_file_id")
    private Set<FilesRepositoryItem> filesRepositoryItems = new HashSet<FilesRepositoryItem>();

    @Override
    public Set<FilesRepositoryItem> getFilesRepositoryItems() {
        return filesRepositoryItems;
    }

    public void setFilesRepositoryItems(Set<FilesRepositoryItem> filesRepositoryItems) {
        this.filesRepositoryItems = filesRepositoryItems;
    }

    @Override
    public String getParentObjectPropertyName() {
        return _PROCESS_INSTANCE_ID;
    }

    @Override
    public Long getParentObjectId() {
        return getProcessInstance().getId();
    }

    @Override
    public void removeItem(FilesRepositoryItem item) {
        filesRepositoryItems.remove(item);
    }
}
