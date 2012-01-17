package pl.net.bluesoft.rnd.processtool.editor.platform.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Random;

public class AperteTokenFilter implements Filter {

    private static final String APERTE_TOKEN_ATTRIBUTE_NAME = "aperteToken";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // setup http client

        // check if the servlet is accessible
        // throw new UnavailableException(10, "Can not connect to Aperte Token Servlet");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        HttpSession session = req.getSession();
        if (session.getAttribute(APERTE_TOKEN_ATTRIBUTE_NAME) == null) {
            // get token from Aperte Token Servlet
            session.setAttribute("aperteToken", Integer.toString(new Random().nextInt()));
        }

        // do forward
        filterChain.doFilter(req, res);
    }

    @Override
    public void destroy() {
       // destroy http client
    }

}
