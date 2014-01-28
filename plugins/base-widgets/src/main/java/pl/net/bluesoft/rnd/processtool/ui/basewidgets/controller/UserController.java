package pl.net.bluesoft.rnd.processtool.ui.basewidgets.controller;

import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

/**
 * @author: mpawlak@bluesoft.net.pl
 */
@OsgiController(name="usercontroller")
public class UserController  implements IOsgiWebController
{
    @Autowired
    protected IPortalUserSource portalUserSource;

    @ControllerMethod(action="getAllUsers")
    public GenericResultBean getSyncStatus(final OsgiWebRequest invocation)
    {
        GenericResultBean result = new GenericResultBean();

        String pageLimit = invocation.getRequest().getParameter("page_limit");
        String queryTerm = invocation.getRequest().getParameter("q");

        result.setData(portalUserSource.findUsers(queryTerm));

        return result;
    }
}
