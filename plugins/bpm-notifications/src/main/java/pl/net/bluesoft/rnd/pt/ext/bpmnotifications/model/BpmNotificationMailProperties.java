package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "pt_ext_bpm_notify_mail_props",
        uniqueConstraints = @UniqueConstraint(columnNames = "profileName"))
public class BpmNotificationMailProperties extends PersistentEntity {
    private String profileName;
    private String smtpHost;
    private String smtpSocketFactoryPort;
    private String smtpSocketFactoryClass;
    private String sslSocketFactoryClass;
    private boolean smtpAuth;
    private boolean disablePlainAuth;
    private String smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private String transportProtocol;
    private boolean starttls;
    private boolean debug;

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpSocketFactoryPort() {
        return smtpSocketFactoryPort;
    }

    public void setSmtpSocketFactoryPort(String smtpSocketFactoryPort) {
        this.smtpSocketFactoryPort = smtpSocketFactoryPort;
    }

    public String getSmtpSocketFactoryClass() {
        return smtpSocketFactoryClass;
    }

    public void setSmtpSocketFactoryClass(String smtpSocketFactoryClass) {
        this.smtpSocketFactoryClass = smtpSocketFactoryClass;
    }

    public boolean isSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(boolean smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpPassword() {
        return smtpPassword;
    }

    public void setSmtpPassword(String smtpPassword) {
        this.smtpPassword = smtpPassword;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

	public boolean isStarttls() {
		return starttls;
	}

	public void setStarttls(boolean starttls) {
		this.starttls = starttls;
	}

	public String getSslSocketFactoryClass() {
		return sslSocketFactoryClass;
	}

	public void setSslSocketFactoryClass(String sslSocketFactoryClass) {
		this.sslSocketFactoryClass = sslSocketFactoryClass;
	}

	public boolean isDisablePlainAuth() {
		return disablePlainAuth;
	}

	public void setDisablePlainAuth(boolean disablePlainAuth) {
		this.disablePlainAuth = disablePlainAuth;
	}

	public String getTransportProtocol() {
		return transportProtocol;
	}

	public void setTransportProtocol(String transportProtocol) {
		this.transportProtocol = transportProtocol;
	}
}