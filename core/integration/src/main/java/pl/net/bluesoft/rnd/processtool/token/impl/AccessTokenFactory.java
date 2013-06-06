package pl.net.bluesoft.rnd.processtool.token.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.token.AccessToken;
import pl.net.bluesoft.rnd.processtool.token.IAccessTokenFactory;

public class AccessTokenFactory implements IAccessTokenFactory {

	@Override
	public AccessToken create(BpmTask userTask, String actionName) 
	{
		/* Get internal task ID */
		String taskIdString = userTask.getInternalTaskId();
		Long taskId = Long.parseLong(taskIdString);
		
		/* Create new token */
		AccessToken accessToken = new AccessToken();
		accessToken.setTaskId(taskId);
		accessToken.setUser(userTask.getAssignee());
		accessToken.setActionName(actionName);
		
		/* Set token value */
		String token = generateToken(taskId);
		accessToken.setToken(token);	
		
		return accessToken;
	}
	
	/** Generate unique token */
	private String generateToken(Long taskId)
	{
        try 
        {
    		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            String token = random.nextLong()*(double)System.nanoTime()+taskId.toString();
            
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            token = toHex(md.digest(token.getBytes()));
            
            return token;
        } 
        catch (NoSuchAlgorithmException e) 
        {
            throw new RuntimeException(e);
        }
	}
	
    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

}
