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
import pl.net.bluesoft.rnd.processtool.usersource.exception.InvalidCredentialsUserSourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    @RequestMapping(method = RequestMethod.POST, value = "/user/login.json")
    @ResponseBody
    public String performAction(final HttpServletRequest request, HttpServletResponse response)
    {
        try {
            String login = request.getParameter("login");
            String password = request.getParameter("password");

            IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);

            UserData user = authorizationService.authenticateByLogin(login,password, request, response);

            return user.getLogin();
        }
        catch(Throwable ex)
        {
             return null;
        }

    }
}
