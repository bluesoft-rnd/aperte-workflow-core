package pl.net.bluesoft.rnd.processtool.plugins;

import java.util.Date;

public class TokenInfo {
   
	private String token;
	private Date creationDate;
	private int validityTime; //in minutes
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public int getValidityTime() {
		return validityTime;
	}
	public void setValidityTime(int validityTime) {
		this.validityTime = validityTime;
	}
	public TokenInfo(String token, Date creationDate, int validityTime) {
		this.token = token;
		this.creationDate = creationDate;
		this.validityTime = validityTime;
	}
	
	
}
