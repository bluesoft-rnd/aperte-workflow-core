package pl.net.bluesoft.rnd.processtool.token;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.TextModes;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;

/**
 * Service for token {@link AccessToken} operations
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface ITokenService 
{
	/**
	 * Get token {@link AccessToken} with given tokenID
	 * 
	 * @param tokenId
	 * @return null if not found
	 */
	AccessToken getTokenByTokenId(String tokenId);
	
	/**
	 * Get already wrapped token by token id
	 * @param tokenId
	 * @return
	 */
	TokenWrapper getTokenWrapperByTokenId(String tokenId);
	
	/**
	 * Wrap {@link AccessToken} to {@link TokenWrapper} witch has
	 * linked {@link UserData} and {@link BpmTask} objects
	 * @param token to wrap
	 * @return wrapped token
	 */
	TokenWrapper wrapAccessToken(AccessToken token);

	
	/**
	 * Delete all tokens with witch belong to 
	 * provided {@link BpmTask} id
	 * 
	 * @param taskId
	 */
	void deleteTokensByTaskId(long taskId);


	/**
	 * Get colletion of {@link AccessToken} object witch belong to 
	 * provided {@link BpmTask} id
	 * 
	 * @param taskId
	 * @return
	 */
	Collection<AccessToken> getAccessTokensByTaskId(long taskId);

	/** Get url address to perform action servlet 
	 * 
	 * @param token tokenId
	 * @param mode 
	 * @return
	 */
	String getPerfromActionServletAddressForToken(String token, TextModes mode);
	
	/** Get url address to fast link servlet */
	String getFastLinkServletAddressForToken(String token);
}
