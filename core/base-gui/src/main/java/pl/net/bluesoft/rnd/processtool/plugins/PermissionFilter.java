package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PermissionFilter implements Filter {

	
	private static final String APERTE_TOKEN_ATTRIBUTE_NAME = "aperteToken";
	
    
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
	    HttpServletResponse res = (HttpServletResponse) response;
	    ServletContext sc = req.getSession().getServletContext();
	    Object obj = sc.getAttribute(TokenServlet.APERTE_TOKEN_MAP);
	    HashMap<String,TokenInfo> tokenMap = null;
	    String token = request.getParameter(APERTE_TOKEN_ATTRIBUTE_NAME);
        
	    //iterate through map and remove expired tokens
		if (obj != null) {
		   tokenMap = (HashMap<String,TokenInfo>)obj;
		   for (String currentToken : tokenMap.keySet()) {
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
	    
        if (tokenMap != null && token != null && tokenMap.get(token) != null) 
        	chain.doFilter(request, response);
        else 
    		res.setStatus(401);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
	
	@Override
	public void destroy() {
	}

}
