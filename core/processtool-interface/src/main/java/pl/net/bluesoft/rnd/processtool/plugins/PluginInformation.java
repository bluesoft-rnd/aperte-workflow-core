package pl.net.bluesoft.rnd.processtool.plugins;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class PluginInformation implements Comparable<PluginInformation> {
    
    private String symbolicName;
    private String description;
    private String homepageUrl;
    private String documentationUrl;
    private String name;
    private String version;

    private long id;
    private int status;
    private String statusDescription;

    private boolean canEnable,canDisable, canUninstall;

    public boolean isCanDisable() {
        return canDisable;
    }

    public void setCanDisable(boolean canDisable) {
        this.canDisable = canDisable;
    }

    public boolean isCanEnable() {
        return canEnable;
    }

    public void setCanEnable(boolean canEnable) {
        this.canEnable = canEnable;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(PluginInformation o) {
        return Long.valueOf(getId()).compareTo(o.getId());
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isCanUninstall() {
        return canUninstall;
    }

    public void setCanUninstall(boolean canUninstall) {
        this.canUninstall = canUninstall;
    }
}
