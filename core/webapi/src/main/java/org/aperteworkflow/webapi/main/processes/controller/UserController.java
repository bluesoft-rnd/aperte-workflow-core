package org.aperteworkflow.webapi.main.processes.controller;

import org.aperteworkflow.webapi.main.AbstractProcessToolServletController;
import org.aperteworkflow.webapi.main.processes.action.domain.PerformActionResultBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * User services controller
 *
 * @author: mpawlak@bluesoft.net.pl
 */
@Controller
public class UserController extends AbstractProcessToolServletController
{
    /**
     * Authenticate user by given login and password
     *
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/login.json")
    @ResponseBody
    public UserData performAction(final HttpServletRequest request)
    {
        String login = request.getParameter("login");
        String password = request.getParameter("password");

        IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);

        UserData user = authorizationService.authenticateByLogin(login,password);

        return user;
    }
}
