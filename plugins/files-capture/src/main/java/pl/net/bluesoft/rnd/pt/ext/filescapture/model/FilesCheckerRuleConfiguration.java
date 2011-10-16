package pl.net.bluesoft.rnd.pt.ext.filescapture.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * Created by Agata Taraszkiewicz
 */
@Entity
@Table(name = "pt_files_checker_rule_config")
public class FilesCheckerRuleConfiguration extends PersistentEntity {
    @Column
    private String repositoryAtomUrl;// = "http://localhost:8080/nuxeo/atom/cmis";

    @Column
    private String repositoryId = "default";

    @Column
    private String repositoryUser = "Administrator";

    @Column
    private String repositoryPassword = "Administrator";

    @Column
    private String rootFolderPath = "/processtool/docs";

    @Column
    private String subFolder = "test1";

    @Column
    private String newFolderPrefix = "pt_";

    @Column
    private String folderAttributeName = "cmisFolderId";

    @ManyToOne
    @JoinColumn(name = "configuration_id")
    private FilesCheckerConfiguration configuration;

    @Column
    private String runningProcessActionName;

    @Column
    private String processIdSubjectLookupRegexp;

    @Column
    private String processTaskName;

    @Column
    private Boolean lookupRunningProcesses;

    public boolean isLookupRunningProcesses() {
        return nvl(lookupRunningProcesses, false);
    }

    public void setLookupRunningProcesses(boolean lookupRunningProcesses) {
        this.lookupRunningProcesses = lookupRunningProcesses;
    }

    public String getProcessIdSubjectLookupRegexp() {
        return processIdSubjectLookupRegexp;
    }

    public void setProcessIdSubjectLookupRegexp(String processIdSubjectLookupRegexp) {
        this.processIdSubjectLookupRegexp = processIdSubjectLookupRegexp;
    }

    public String getRepositoryAtomUrl() {
        return repositoryAtomUrl;
    }

    public void setRepositoryAtomUrl(String repositoryAtomUrl) {
        this.repositoryAtomUrl = repositoryAtomUrl;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        this.repositoryUser = repositoryUser;
    }

    public String getRepositoryPassword() {
        return repositoryPassword;
    }

    public void setRepositoryPassword(String repositoryPassword) {
        this.repositoryPassword = repositoryPassword;
    }

    public String getRootFolderPath() {
        return rootFolderPath;
    }

    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }

    public String getNewFolderPrefix() {
        return newFolderPrefix;
    }

    public void setNewFolderPrefix(String newFolderPrefix) {
        this.newFolderPrefix = newFolderPrefix;
    }

    public String getFolderAttributeName() {
        return folderAttributeName;
    }

    public void setFolderAttributeName(String folderAttributeName) {
        this.folderAttributeName = folderAttributeName;
    }

    public FilesCheckerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(FilesCheckerConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getRunningProcessActionName() {
        return runningProcessActionName;
    }

    public void setRunningProcessActionName(String runningProcessActionName) {
        this.runningProcessActionName = runningProcessActionName;
    }

    public String getProcessTaskName() {
        return processTaskName;
    }

    public void setProcessTaskName(String processTaskName) {
        this.processTaskName = processTaskName;
    }
}
