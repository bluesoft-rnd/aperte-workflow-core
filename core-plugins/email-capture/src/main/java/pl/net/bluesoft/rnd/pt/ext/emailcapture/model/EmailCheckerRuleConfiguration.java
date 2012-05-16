package pl.net.bluesoft.rnd.pt.ext.emailcapture.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name="pt_email_checker_rule_config")
public class EmailCheckerRuleConfiguration extends PersistentEntity {
	/**
	 * Regular expression that matches email subject. If the subject does not match, email is ignored for
	 * this rule.
	 *
	 * Empty field means that subject is not validated.
	 */
	@Column
	private String subjectRegexp;

	/**
	 * Regular expression that matches email sender (name and email address). If the sender does not match, email is ignored for
	 * this rule.
	 *
	 * Empty field means that sender is not validated.
	 */
	@Column
	private String senderRegexp;


	/**
	 * Regular expression that matches email recipient (name and email address). If the recipient does not match, email is ignored for
	 * this rule.
	 *
	 * Empty field means that recipient is not validated.
	 */
	@Column
	private String recipientRegexp;

	/**
	 * Process code to start. Must match ProcessDefinitionConfig.bpmDefinitionKey value.
	 */
	@Column
	private String processCode;

	/**
	 * should attachments of unknown or text/* content-type be added published do CMIS
	 */
	@Column
	private Boolean omitTextAttachments;

	/**
	 * Lookup running processes. The running process is looked up using email subject (with removed tokens
	 * provided by subjectRemovables attribute, removed whitespaces and uppercased) matched to ProcessInstance.keyword.
	 *
	 * When the process is initiated by email, ProcessInstance.keyword is filled with email subject using the algorithm
	 * presented above.
	 *
	 * What may be interesting is the fact, that one rule can start process, and another rule can lookup matching
	 * instance later on and publish attachments to another cmis folder.
	 */
	@Column
	private Boolean lookupRunningProcesses;

	/**
	 * When no running process has been matched, should a new process be started?
	 */
	@Column
	private Boolean startNewProcesses;

	/**
	 * Tokens removed from subject. Used for matching of processes using lookupRunningProcess mechanism.
	 */
	@Column
	private String subjectRemovables;

	/**
	 * CMIS repository Atom url. If not provided, email attachments are ignored and not stored in CMIS.
	 */
	@Column
	private String repositoryAtomUrl;// = "http://localhost:8080/nuxeo/atom/cmis";

	/**
	 * CMIS repository ID.
	 */
	@Column
	private String repositoryId = "default";

	/**
	 * CMIS repository username
	 */
	@Column
	private String repositoryUser = "Administrator";
	/**
	 * CMIS repository password
	 */
	@Column
	private String repositoryPassword = "Administrator";

	/**
	 * CMIS repository root folder path. In this root folder, a folder with process name and prefixed with newFolderPrefix
	 * is created. In this folder, a sub-folder with a name subFolder is created and email attachments are stored there.
	 *
	 * If the folders on the path do not exist, the mechanism attempts to create them.
	 */
	@Column
	private String rootFolderPath = "/processtool/docs";

	/**
	 * Subfolder name. Documents are stored in ${rootFolderPath}/${newFolderPrefix}${processCode}/${subFolder}/
	 */
	@Column
	private String subFolder = "test1";

	/**
	 * Prefix used when creating a folder dedicated to a process instance on CMIS.
	 * Documents are stored in ${rootFolderPath}/${newFolderPrefix}${processCode}/${subFolder}/
	 */
	@Column
	private String newFolderPrefix = "pt_";

	/**
	 * Under which process attribute a cmis folder ID for the documents is stored. Use of this attribute is suggested
	 * only when process uses a single CMIS folder (without subfolders).
	 */
	@Column
	private String folderAttributeName = "cmisFolderId";

	@ManyToOne
	@JoinColumn(name="configuration_id")
	private EmailCheckerConfiguration configuration;

	/**
	 * When processIdSubjectLookupRegexp is defined and matched and also matching process has been found
	 * and matches field processStepName. This action will be performed on the process, propagating further its
	 * execution.
	 *
	 * The process to be propagated further has to be waiting in a task in a special task.
	 *
	 * It is worth mentioning, that different combinations of runningProcessActionName and
	 * processIdSubjectLookupRegexp can provide simple decisions for a process.
	 *
	 * Email's sender address will be marked as a decision source, so there should exist some kind of regexp on sender's email and
	 * some verification of sender (e.g. SMTP AUTH) should be used.
	 */
	@Column
	private String runningProcessActionName;

	/**
	 * When processIdSubjectLookupRegexp is defined and matched and also matching process has been found
	 * and matches field processStepName. This action will be performed on the process, propagating further its
	 * execution.                     \
	 *
	 * The regexp should contain one grouping, which will allow the engine to extract ProcessInstance.externalKey
	 *
	 * The process to be propagated further has to be waiting in a task in a special task.
	 *
	 * It is worth mentioning, that different combinations of runningProcessActionName and
	 * processIdSubjectLookupRegexp can provide simple decisions for a process.
	 *
	 * Email's sender address will be marked as a decision source, so there should exist some kind of regexp on sender's email and
	 * some verification of sender (e.g. SMTP AUTH) should be used.
	 */
	@Column
	private String processIdSubjectLookupRegexp;

	/**
	 * When processIdSubjectLookupRegexp is defined and matched and also matching process has been found
	 * and matches field processStepName. This action will be performed on the process, propagating further its
	 * execution.
	 *
	 * The process to be propagated further has to be waiting in a task in a special task.
	 *
	 * It is worth mentioning, that different combinations of runningProcessActionName and
	 * processIdSubjectLookupRegexp can provide simple decisions for a process.
	 *
	 * Email's sender address will be marked as a decision source, so there should exist some kind of regexp on sender's email and
	 * some verification of sender (e.g. SMTP AUTH) should be used.
	 */
	@Column
	private String processTaskName;

	public boolean isLookupRunningProcesses() {
		return nvl(lookupRunningProcesses, false);
	}

	public void setLookupRunningProcesses(boolean lookupRunningProcesses) {
		this.lookupRunningProcesses = lookupRunningProcesses;
	}

	public Boolean getStartNewProcesses() {
		return nvl(startNewProcesses, false);
	}

	public void setStartNewProcesses(Boolean startNewProcesses) {
		this.startNewProcesses = startNewProcesses;
	}

	public String getSubjectRemovables() {
		return subjectRemovables;
	}

	public void setSubjectRemovables(String subjectRemovables) {
		this.subjectRemovables = subjectRemovables;
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

	public String getSubjectRegexp() {
		return subjectRegexp;
	}

	public void setSubjectRegexp(String subjectRegexp) {
		this.subjectRegexp = subjectRegexp;
	}

	public String getSenderRegexp() {
		return senderRegexp;
	}

	public void setSenderRegexp(String senderRegexp) {
		this.senderRegexp = senderRegexp;
	}

	public String getRecipientRegexp() {
		return recipientRegexp;
	}

	public void setRecipientRegexp(String recipientRegexp) {
		this.recipientRegexp = recipientRegexp;
	}

	public String getProcessCode() {
		return processCode;
	}

	public void setProcessCode(String processCode) {
		this.processCode = processCode;
	}

	public Boolean getOmitTextAttachments() {
		return omitTextAttachments;
	}

	public Boolean getLookupRunningProcesses() {
		return lookupRunningProcesses;
	}

	public boolean isOmitTextAttachments() {
		return nvl(omitTextAttachments, false);
	}

	public void setOmitTextAttachments(boolean omitTextAttachments) {
		this.omitTextAttachments = omitTextAttachments;
	}

	public EmailCheckerConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(EmailCheckerConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getRunningProcessActionName() {
		return runningProcessActionName;
	}

	public void setRunningProcessActionName(String runningProcessActionName) {
		this.runningProcessActionName = runningProcessActionName;
	}

	public String getProcessIdSubjectLookupRegexp() {
		return processIdSubjectLookupRegexp;
	}

	public void setProcessIdSubjectLookupRegexp(String processIdSubjectLookupRegexp) {
		this.processIdSubjectLookupRegexp = processIdSubjectLookupRegexp;
	}

	public String getProcessTaskName() {
		return processTaskName;
	}

	public void setProcessTaskName(String processTaskName) {
		this.processTaskName = processTaskName;
	}
}
