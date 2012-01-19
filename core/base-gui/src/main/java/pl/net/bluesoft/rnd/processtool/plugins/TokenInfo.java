package pl.net.bluesoft.rnd.processtool.plugins;

import java.util.Date;

public class TokenInfo {
   
	private String token;
    private String userLogin;
	private Date creationDate;
	private int validityTime; //in minutes

    public TokenInfo() {
    }

    public TokenInfo(String token, String userLogin, Date creationDate, int validityTime) {
        this.token = token;
        this.userLogin = userLogin;
        this.creationDate = creationDate;
        this.validityTime = validityTime;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

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
