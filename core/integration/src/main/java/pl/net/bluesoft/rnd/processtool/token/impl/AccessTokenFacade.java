package pl.net.bluesoft.rnd.processtool.token.impl;

import static pl.net.bluesoft.util.lang.Strings.withEnding;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.BasicSettings;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.TextModes;
import pl.net.bluesoft.rnd.processtool.facade.AbstractFacade;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.token.ITokenService;
import pl.net.bluesoft.rnd.processtool.token.TokenWrapper;
import pl.net.bluesoft.rnd.processtool.token.callbacks.DeleteAccessTokenByTaskIdCallback;
import pl.net.bluesoft.rnd.processtool.token.callbacks.GetAccessTokenCallback;
import pl.net.bluesoft.rnd.processtool.token.callbacks.GetAccessTokensByTaskIdCallback;
import pl.net.bluesoft.rnd.processtool.token.callbacks.WrapAccessTokenCallback;
import pl.net.bluesoft.rnd.processtool.token.exception.TokenNotFoundException;
import pl.net.bluesoft.util.lang.Strings;


/**
 * Access Token Facade for operations using {@link AccessToken}
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class AccessTokenFacade extends AbstractFacade implements ITokenService
{	
    private Logger logger = Logger.getLogger(AccessTokenFacade.class.getName());
    
	/** Get {@link AccessToken} object using provided token*/
	public AccessToken getTokenByTokenId(String tokenId)
	{
		GetAccessTokenCallback getAccessTokenCallback = new GetAccessTokenCallback(tokenId);
		
		return processCallback(getAccessTokenCallback);
	}
	
	public void deleteTokensByTaskId(long taskId)
	{
		DeleteAccessTokenByTaskIdCallback deleteAccessTokenByTaskIdCallback = new DeleteAccessTokenByTaskIdCallback(taskId);
		
		processCallback(deleteAccessTokenByTaskIdCallback);
	}

	public Collection<AccessToken> getAccessTokensByTaskId(long internalTaskId) 
	{
		logger.log(Level.INFO, "Get token callback by taskid:  ", internalTaskId);
		GetAccessTokensByTaskIdCallback getAccessTokensByTaskIdCallback = new GetAccessTokensByTaskIdCallback(internalTaskId);
		
		return processCallback(getAccessTokensByTaskIdCallback);
		
	}
	
	@Override
	public TokenWrapper wrapAccessToken(AccessToken token) 
	{
		WrapAccessTokenCallback wrapAccessTokenCallback = new WrapAccessTokenCallback(token);
		return processCallback(wrapAccessTokenCallback);
	}
	
	@Override
	public TokenWrapper getTokenWrapperByTokenId(String tokenId) 
	{
		AccessToken accessToken = getTokenByTokenId(tokenId);
		if(accessToken == null)
			throw new TokenNotFoundException("No token was found for [tokenId="+tokenId+"]");
		
		return wrapAccessToken(accessToken);
	}
	
	/** Get url address to perform action servlet */
	public String getPerformActionServletFullAddress()
	{
		String portletUrl = getSetting(BasicSettings.ACTIVITY_PORTLET_URL);
		
		String url = Strings.hasLength(portletUrl) ? withEnding(portletUrl, ProcessToolContext.TOKEN_SERVLET_URL) : null;
		
		return url;
	}
	
	/** Get url address to perform action servlet */
	public String getFastLinkServletAddress()
	{
		String applicationUrl = getSetting(BasicSettings.ACTIVITY_STANDALONE_SERVLET_URL);
		
		return applicationUrl;
	}
	
	/** Get url address to perform action servlet for given token */
	public String getPerfromActionServletAddressForToken(String tokenId, TextModes mode)
	{
		String servletUrl = getPerformActionServletFullAddress();
		if(servletUrl == null)
			return "";
		
		servletUrl = Strings.withRequestParameter(servletUrl, ProcessToolBpmConstants.REQUEST_PARAMETER_TOKEN_ID, tokenId);
		servletUrl = Strings.withRequestParameter(servletUrl, ProcessToolBpmConstants.TEXT_MODE, mode.toString());
		
		return servletUrl;
	}

	@Override
	public String getFastLinkServletAddressForToken(String tokenId) 
	{
		String applicationUrl = getFastLinkServletAddress();
		
		return applicationUrl != null ? Strings.withRequestParameter(applicationUrl, ProcessToolBpmConstants.REQUEST_PARAMETER_TOKEN_ID, tokenId) : "";
	}


}
