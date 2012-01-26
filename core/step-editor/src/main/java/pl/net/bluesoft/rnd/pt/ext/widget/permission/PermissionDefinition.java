package pl.net.bluesoft.rnd.pt.ext.widget.permission;

public class PermissionDefinition {

    private String key;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PermissionDefinition)) return false;

        PermissionDefinition that = (PermissionDefinition) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
