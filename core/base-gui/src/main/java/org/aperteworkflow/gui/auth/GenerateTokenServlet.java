package org.aperteworkflow.gui.auth;

import pl.net.bluesoft.rnd.processtool.plugins.PermissionFilter;
import pl.net.bluesoft.rnd.processtool.plugins.TokenInfo;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class GenerateTokenServlet extends HttpServlet {

    //test url:
    //http://localhost:8080/jbpm-gui-0.1/g_token?returl=http://localhost:8080/jbpm-gui-0.1/g_token?token=
    private static final String LAST_TOKEN = "__APERTE__LAST_TOKEN";
    private static final String TOKEN_MAP = "__APERTE__TOKEN_MAP";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ServletContext sc = req.getSession().getServletContext();
        HashMap<String, TokenInfo> tokenMap = getTokenMap(sc);
        cleanupTokens(tokenMap);
        if (req.getParameter("token") == null && req.getParameter("returl") != null) {
            String token = Math.random()*(double)System.nanoTime()+req.toString();
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                token = toHex(md.digest(token.getBytes()));
                req.getSession().setAttribute(LAST_TOKEN, token);
                resp.sendRedirect(req.getParameter("returl") + token);
                tokenMap.put(token,
                        new TokenInfo(token,
                                (String)req.getSession().getAttribute(PermissionFilter.AUTHORIZED),
                        new Date(),
                        1));
            } catch (NoSuchAlgorithmException e) {
                throw new ServletException(e);
            }
        } else if (req.getParameter("token") != null && req.getParameter("returl") == null) {
            resp.setContentType("text/plain");
            TokenInfo ti = tokenMap.get(req.getParameter("token"));
            if (ti != null && ti.getUserLogin() != null) {
                resp.getWriter().print(ti.getUserLogin());
            } else {
                resp.setStatus(401);
                resp.getWriter().print("Invalid token");
            }
        } else {
            resp.getWriter().print("invalid syntax, please consult source code for org.aperteworkflow.gui.auth.GenerateTokenServlet");
        }

    }

    private static synchronized HashMap<String, TokenInfo> getTokenMap(ServletContext sc) {
        HashMap<String,TokenInfo> tokenMap = (HashMap<String, TokenInfo>) sc.getAttribute(TOKEN_MAP);
        if (tokenMap == null) {
            tokenMap = new HashMap<String, TokenInfo>();
            sc.setAttribute(TOKEN_MAP, tokenMap);
        }
        return tokenMap;
    }

    private static synchronized void cleanupTokens(HashMap<String, TokenInfo> tokenMap) {

        Collection<String> tokens = new HashSet<String>(tokenMap.keySet());//avoid concurrent modification exception, please!

        for (String currentToken : tokens) {
            TokenInfo tokenInfo = tokenMap.get(currentToken);
            if (tokenInfo != null) {
                int validityTime = tokenInfo.getValidityTime();
                Date creationDate = tokenInfo.getCreationDate();
                Calendar validityCalendar = Calendar.getInstance();
                validityCalendar.setTime(creationDate);
                validityCalendar.add(Calendar.MINUTE, validityTime);
                if (new Date().after(validityCalendar.getTime())) {
                    tokenMap.remove(currentToken);
                }
            } else {
                tokenMap.remove(currentToken);
            }
        }
    }


    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }
}
