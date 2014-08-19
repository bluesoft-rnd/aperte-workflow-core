package pl.net.bluesoft.rnd.processtool.ui.basewidgets.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
@OsgiController(name="usercontroller")
public class UserController  implements IOsgiWebController
{
    @Autowired
    protected IPortalUserSource portalUserSource;

    @Autowired
    protected IUserRolesManager userRolesManager;

    @ControllerMethod(action="getAllUsers")
    public GenericResultBean getSyncStatus(final OsgiWebRequest invocation)
    {
        GenericResultBean result = new GenericResultBean();

        String pageLimit = invocation.getRequest().getParameter("page_limit");
        String queryTerm = invocation.getRequest().getParameter("q");
        String rolesString = invocation.getRequest().getParameter("roles");

        Collection<String> roles = new ArrayList<String>();

        if(rolesString != null && !rolesString.isEmpty())
            roles = Arrays.asList(StringUtils.split(rolesString, ","));

        Collection<UserData> users =  portalUserSource.getAllUsers();

        if((queryTerm == null || queryTerm.isEmpty()) && roles.isEmpty())
        {
            result.setData(users);

            return result;
        }


        Collection<UserData> filtered = new LinkedList<UserData>();
        for(UserData user: users)
        {
            if(!user.getRealName().toLowerCase().contains(queryTerm.toLowerCase()) &&
                    !user.getLogin().toLowerCase().contains(queryTerm.toLowerCase()))
                continue;

            if(!roles.isEmpty() && !isUserHavingOneOfRoles(roles, user))
                continue;

            /* All filter conditions met, add user */
            filtered.add(user);
        }

        result.setData(filtered);

        return result;
    }

    private boolean isUserHavingOneOfRoles(Collection<String> roles, UserData user)
    {
        for(String roleName: roles)
            if(user.getRoles().contains(roleName))
                return true;

        return false;
    }

    @ControllerMethod(action="getUsersWithRoles")
    public GenericResultBean getUsersWithRoles(final OsgiWebRequest invocation)
    {
        GenericResultBean result = new GenericResultBean();

        String pageLimit = invocation.getRequest().getParameter("page_limit");
        String queryTerm = invocation.getRequest().getParameter("q");
        String roleName = invocation.getRequest().getParameter("roleName");

        Collection<UserData> users =  userRolesManager.getUsersByRole(roleName);

        if(queryTerm == null || queryTerm.isEmpty())
        {
            result.setData(users);

            return result;
        }


        Collection<UserData> filtered = new LinkedList<UserData>();
        for(UserData user: users)
        {
            if(user.getRealName().toLowerCase().contains(queryTerm.toLowerCase()) ||
                    user.getLogin().toLowerCase().contains(queryTerm.toLowerCase()))
                filtered.add(user);
        }

        result.setData(filtered);

        return result;
    }


    @ControllerMethod(action = "getUserByLogin")
    public GenericResultBean getUserByLogin(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();

        String userLogin = invocation.getRequest().getParameter("userLogin");

        UserData user = portalUserSource.getUserByLogin(userLogin);

        result.setData(user);

        return result;
    }
}
