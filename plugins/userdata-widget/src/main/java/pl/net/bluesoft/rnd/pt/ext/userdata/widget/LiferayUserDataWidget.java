package pl.net.bluesoft.rnd.pt.ext.userdata.widget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name="LiferayUserData")
@WidgetGroup("userdata-widget")
public class LiferayUserDataWidget extends UserDataWidget
{

    @Autowired
    private IUserRolesManager userRolesManager;


	@AutoWiredProperty
    @AperteDoc(
            humanNameKey = "userdata.widget.liferay.liferayRoleName",
            descriptionKey = "userdata.widget.liferay.liferayRoleName.description"
    )
	public String liferayRoleName = "User";

    public LiferayUserDataWidget()
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

	@Override
	protected Collection<UserData> getUsers()
    {
        HashSet<UserData> users = new HashSet<UserData>();
        Collection<pl.net.bluesoft.rnd.processtool.model.UserData> aperteUsers =
                userRolesManager.getUsersByRole(liferayRoleName);
        for (pl.net.bluesoft.rnd.processtool.model.UserData aperteUser : aperteUsers) {

            UserData ud = new UserData();
            ud.setLogin(aperteUser.getLogin());
            ud.setDescription(aperteUser.getRealName());
            ud.setBpmLogin(aperteUser.getLogin());
            users.add(ud);
        }
        return users;

	}

	public String getLiferayRoleName() {
		return liferayRoleName;
	}

	public void setLiferayRoleName(String liferayRoleName) {
		this.liferayRoleName = liferayRoleName;
	}
}
