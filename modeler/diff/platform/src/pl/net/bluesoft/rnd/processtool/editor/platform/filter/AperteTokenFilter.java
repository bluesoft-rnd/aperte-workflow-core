package pl.net.bluesoft.rnd.processtool.editor.platform.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.signavio.platform.core.Platform;
import com.signavio.platform.core.PlatformProperties;

public class AperteTokenFilter implements Filter {

    private static final String APERTE_TOKEN_ATTRIBUTE_NAME = "aperteToken";
    private static final Logger logger = Logger.getLogger(AperteTokenFilter.class);
    
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
            session.setAttribute(APERTE_TOKEN_ATTRIBUTE_NAME, getAperteToken());
        }

        // do forward
        filterChain.doFilter(req, res);
    }

    @Override
    public void destroy() {
       // destroy http client
    }

    private String getAperteToken() {
    	 PlatformProperties props = Platform.getInstance().getPlatformProperties();
     	 String tokenUrl = props.getServerName() + props.getJbpmGuiUrl() + "/token?9d20dc34-6f15-4650-a8e6-cb6292e7a729=8ab24b1f-1229-34fe-ae94-a359b31e821c";
         try {
         	URL url = new URL(tokenUrl);
 	        URLConnection conn = url.openConnection();
 	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 	        StringBuffer sb = new StringBuffer();
 	        String line;
 	        while ((line = rd.readLine()) != null)
 	        {
 	            sb.append(line);
 	        }
 	        rd.close();
 	        return sb.toString();
         } catch (IOException e) {
         	logger.error("Error reading data from " + tokenUrl, e);
         	return null;
         }
    }
}
