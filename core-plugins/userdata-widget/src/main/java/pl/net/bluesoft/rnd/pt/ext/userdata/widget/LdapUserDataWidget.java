package pl.net.bluesoft.rnd.pt.ext.userdata.widget;

import com.novell.ldap.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="LdapUserData")
@WidgetGroup("userdata-widget")
public class LdapUserDataWidget extends UserDataWidget {

	@AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.ip",
            descriptionKey = "userdata.widget.ldap.ip.description"
    )
	public String ip;

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.port",
            descriptionKey = "userdata.widget.ldap.port.description"
    )
    public Integer port = 389;

	@AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.loginDN",
            descriptionKey = "userdata.widget.ldap.loginDN.description"
    )
	public String loginDN;

	@AutoWiredProperty(required = true)
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.password",
            descriptionKey = "userdata.widget.ldap.password.description"
    )
    public String password;

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.searchBase",
            descriptionKey = "userdata.widget.ldap.searchBase.description"
    )
	public String searchBase = "";

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.searchFilter",
            descriptionKey = "userdata.widget.ldap.searchFilter.description"
    )
	public String searchFilter = "(objectClass=inetOrgPerson)";

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.loginAttr",
            descriptionKey = "userdata.widget.ldap.loginAttr.description"
    )
	public String loginAttr = "uid";

	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.ldap.descriptionAttr",
            descriptionKey = "userdata.widget.ldap.descriptionAttr.description"
    )
	public String descriptionAttr = "cn";
	

	private LDAPConnection getLdapConnection() {
		try {
			LDAPConnection lc = new LDAPConnection();
			lc.connect(ip, port);
			lc.bind(LDAPConnection.LDAP_V3, loginDN, password.getBytes("UTF8"));
			return lc;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Collection<UserData> getUsers() {
		Collection<UserData> users = new HashSet();
		LDAPConnection ldapConnection = getLdapConnection();
		try {
			try {
				LDAPSearchResults results = ldapConnection.search(searchBase, LDAPConnection.SCOPE_SUB,
				                                                  searchFilter, null, false);
				while (results.hasMore()) {
					LDAPEntry ldapEntry = results.next();
					LDAPAttributeSet attrs = ldapEntry.getAttributeSet();
					UserData ud = new UserData();
					if (attrs.getAttribute(loginAttr) == null) continue;
					String login = attrs.getAttribute(loginAttr).getStringValue();
					ud.setLogin(ldapEntry.getDN() + ", " + loginAttr + "=" + login);
					if (attrs.getAttribute(descriptionAttr) == null) {
						ud.setDescription(ud.getLogin());	
					} else {
						ud.setDescription(attrs.getAttribute(descriptionAttr).getStringValue());
					}
					ud.setBpmLogin(login);
					users.add(ud);
				}
			}
			finally {
					ldapConnection.disconnect();
			}
		} catch (LDAPException e) {
			throw new RuntimeException(e);
		}

		return users;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getLoginDN() {
		return loginDN;
	}

	public void setLoginDN(String loginDN) {
		this.loginDN = loginDN;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSearchBase() {
		return searchBase;
	}

	public void setSearchBase(String searchBase) {
		this.searchBase = searchBase;
	}

	public String getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(String searchFilter) {
		this.searchFilter = searchFilter;
	}

	public String getLoginAttr() {
		return loginAttr;
	}

	public void setLoginAttr(String loginAttr) {
		this.loginAttr = loginAttr;
	}

	public String getDescriptionAttr() {
		return descriptionAttr;
	}

	public void setDescriptionAttr(String descriptionAttr) {
		this.descriptionAttr = descriptionAttr;
	}
}
