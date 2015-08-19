package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

/**
 * Created by Maciej on 2014-11-12.
 */
public class IMAPMailAccountProperties
{
    private String profileName;

    private String mailProtocol;

    private String mail;

    private String mailHost;

    private String mailPort;

    private String mailUser;

    private String mailPass;

    private String mailToProcessFolder;

    private String mailProcessingFolder;

    private String mailProcessedFolder;

    private String mailErrorFolder;

    private String mailStoreClass;

    private String mailAuthMechanism;

    private String mailNTLMDomain;

    private String mailSocketFactoryClass;

    private String partialFetch;

    private String fetchSize;

    private String timeout;

    public String getMailProtocol() {
        return mailProtocol;
    }

    public void setMailProtocol(String mailProtocol) {
        this.mailProtocol = mailProtocol;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String getMailPort() {
        return mailPort;
    }

    public void setMailPort(String mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    public String getMailPass() {
        return mailPass;
    }

    public void setMailPass(String mailPass) {
        this.mailPass = mailPass;
    }

    public String getMailToProcessFolder() {
        return mailToProcessFolder;
    }

    public void setMailToProcessFolder(String mailToProcessFolder) {
        this.mailToProcessFolder = mailToProcessFolder;
    }

    public String getMailProcessedFolder() {
        return mailProcessedFolder;
    }

    public void setMailProcessedFolder(String mailProcessedFolder) {
        this.mailProcessedFolder = mailProcessedFolder;
    }

    public String getMailErrorFolder() {
        return mailErrorFolder;
    }

    public void setMailErrorFolder(String mailErrorFolder) {
        this.mailErrorFolder = mailErrorFolder;
    }

    public String getMailStoreClass() {
        return mailStoreClass;
    }

    public void setMailStoreClass(String mailStoreClass) {
        this.mailStoreClass = mailStoreClass;
    }

    public String getMailAuthMechanism() {
        return mailAuthMechanism;
    }

    public void setMailAuthMechanism(String mailAuthMechanism) {
        this.mailAuthMechanism = mailAuthMechanism;
    }

    public String getMailNTLMDomain() {
        return mailNTLMDomain;
    }

    public void setMailNTLMDomain(String mailNTLMDomain) {
        this.mailNTLMDomain = mailNTLMDomain;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getMailSocketFactoryClass() {
        return mailSocketFactoryClass;
    }

    public void setMailSocketFactoryClass(String mailSocketFactoryClass) {
        this.mailSocketFactoryClass = mailSocketFactoryClass;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMailProcessingFolder() {
        return mailProcessingFolder;
    }

    public void setMailProcessingFolder(String mailProcessingFolder) {
        this.mailProcessingFolder = mailProcessingFolder;
    }

    public String getPartialFetch() {
        return partialFetch;
    }

    public void setPartialFetch(String partialFetch) {
        this.partialFetch = partialFetch;
    }

    public String getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(String fetchSize) {
        this.fetchSize = fetchSize;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
}
