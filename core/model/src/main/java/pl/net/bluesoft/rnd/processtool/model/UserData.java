package pl.net.bluesoft.rnd.processtool.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author tlipski@bluesoft.net.pl
 */

public interface UserData {
	String _LOGIN = "login";
	String _REAL_NAME = "realName";
	String _FILTERED_NAME = "filteredName";

	String getLogin();
    String getFirstName();
	String getLastName();
	String getRealName();
	String getFilteredName();

	String getEmail();
	String getJobTitle();
	Long getCompanyId();
    Object getAttribute(String key);
    Map<String, Object> getAttributes();

	Set<String> getRoles();
	boolean hasRole(String roleName);
}
