package pl.net.bluesoft.rnd.processtool.plugins;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import pl.net.bluesoft.util.lang.StringUtil;




public class TokenServlet extends HttpServlet {

	private static final long serialVersionUID = -6367947145825998998L;
    public static final String APERTE_TOKEN_MAP = "APERTE_TOKEN_MAP";
    private static final int TOKEN_VALIDITY_TIME = 15; //in minutes
	
    private String secureKey;
    private String secureValue;
    
    @Override
    public void init() throws ServletException {
    	secureKey = getServletConfig().getInitParameter("secure-key");
    	secureValue = getServletConfig().getInitParameter("secure-value");
    }
    
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	   
	   ServletContext sc = req.getSession().getServletContext();
	   HashMap<String,TokenInfo> tokenMap;
	   if (sc.getAttribute(APERTE_TOKEN_MAP) == null) {
		   tokenMap = new HashMap<String,TokenInfo>();
		   sc.setAttribute(APERTE_TOKEN_MAP, tokenMap);
	   } else {
		   tokenMap = (HashMap<String,TokenInfo>)(sc.getAttribute(APERTE_TOKEN_MAP));
	   }
	   
	   String secureKeyValue = req.getParameter(secureKey);
	   String token = UUID.randomUUID().toString();
	   if (secureValue.equals(secureKeyValue)) {
	     tokenMap.put(token, new TokenInfo(token, new Date(), TOKEN_VALIDITY_TIME));
	   }
		
	   PrintWriter out = resp.getWriter();
	   out.println(token);
	   out.close();
   }	
	
}
