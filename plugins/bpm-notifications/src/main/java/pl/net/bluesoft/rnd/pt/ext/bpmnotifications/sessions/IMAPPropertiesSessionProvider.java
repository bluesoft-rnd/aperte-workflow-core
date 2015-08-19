package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.IMAPMailAccountProperties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-01-22.
 */
public class IMAPPropertiesSessionProvider implements IIMAPMailSessionProvider
{
    private static Cipher cipher = null;
    private static Base64 base64 = new Base64(true);

    private static final String mailKey = "dsFgSh3t=ds23d=";

    private static final Logger logger = Logger.getLogger(IMAPPropertiesSessionProvider.class.getName());

    private Map<String, IMAPMailAccountProperties> getMailAccountProperties() {

        Map<String, IMAPMailAccountProperties> mailAccountPropertieses = new HashMap<String, IMAPMailAccountProperties>();

        String propertyFilePath = System.getProperty("exchange.imap.properties.file");

        if(propertyFilePath == null)
            throw new RuntimeException("There is no exchange.imap.properties.file property set in system!");

        try {
            InputStream input = new FileInputStream(propertyFilePath);

            Properties props = new Properties();
            props.load(input);

            String accountsString = props.getProperty("exchange.accounts");
            Collection<String> accounts = Arrays.asList(StringUtils.split(accountsString, ","));

            logger.finest("[IMAP] Properties loaded for accounts");

            for (String accountName : accounts) {

                IMAPMailAccountProperties accountProperties = new IMAPMailAccountProperties();

                String profileName = getPrefixedProperty("profile.name", accountName, props);
                accountProperties.setProfileName(profileName);
                accountProperties.setMail(getPrefixedProperty("exchange.accounts.mail", accountName, props));
                accountProperties.setMailErrorFolder(getPrefixedProperty("exchange.accounts.errorFolder", accountName, props));
                accountProperties.setMailProcessingFolder(getPrefixedProperty("exchange.accounts.processingFolder", accountName, props));
                accountProperties.setMailProcessedFolder(getPrefixedProperty("exchange.accounts.processedFolder", accountName, props));
                accountProperties.setMailToProcessFolder(getPrefixedProperty("exchange.accounts.toProcessFolder", accountName, props));
                accountProperties.setMailHost(getPrefixedProperty("exchange.accounts.mailHost", accountName, props));
                accountProperties.setMailUser(getPrefixedProperty("exchange.accounts.user", accountName, props));
                accountProperties.setMailPass(getPrefixedProperty("exchange.accounts.password", accountName, props));
                accountProperties.setMailProtocol(getPrefixedProperty("exchange.accounts.store.protocol", accountName, props));
                accountProperties.setTimeout(getPrefixedProperty("exchange.accounts.timeout", accountName, props));
                accountProperties.setMailPort(getPrefixedProperty("exchange.accounts.store.port", accountName, props));
                accountProperties.setMailSocketFactoryClass(getPrefixedProperty("exchange.accounts.store.socketFactory.class", accountName, props));
                accountProperties.setMailAuthMechanism(getPrefixedProperty("exchange.accounts.auth.mechanisms", accountName, props));
                accountProperties.setMailNTLMDomain(getPrefixedProperty("exchange.accounts.store.ntlm.domain", accountName, props));
                accountProperties.setMailStoreClass(getPrefixedProperty("exchange.accounts.imap.class", accountName, props));
                accountProperties.setPartialFetch(getPrefixedProperty("exchange.accounts.imap.partialfetch", accountName, props));
                accountProperties.setFetchSize(getPrefixedProperty("exchange.accounts.imap.fetchSize", accountName, props));

                mailAccountPropertieses.put(profileName, accountProperties);
            }
        }
        catch (Throwable ex)
        {
            logger.log(Level.SEVERE, "Problem during obtaining imap properties", ex);
            return new HashMap<String, IMAPMailAccountProperties>();
        }

        return mailAccountPropertieses;

    }

    public IMAPMailAccountProperties getProperties(String profileName)
    {
        return getMailAccountProperties().get(profileName);
    }

    public Store connect(String profile) throws Exception
    {
        IMAPMailAccountProperties mailAccountProperties = getMailAccountProperties().get(profile);
        if(mailAccountProperties == null)
            throw new RuntimeException("No IMAP profile [profileName="+profile+"]");

        Properties props = new Properties();

        String user = mailAccountProperties.getMailUser();
        String email = mailAccountProperties.getMail();
        String encryptedPassword = mailAccountProperties.getMailPass();
        String host = mailAccountProperties.getMailHost();
        String port = mailAccountProperties.getMailPort();

        String partialFetch = mailAccountProperties.getPartialFetch();
        String fetchSize = mailAccountProperties.getFetchSize();

        String decryptedPassword = decryptPassword(encryptedPassword);

        props.setProperty("mail.store.protocol", mailAccountProperties.getMailProtocol());
        props.setProperty("mail.imap.port", port);
        props.setProperty("mail.imap.user", user);
        props.setProperty("mail.imap.host", host);
        props.setProperty("mail.imap.password", decryptedPassword);
        //props.setProperty("mail.debug", "true");
        props.setProperty("mail.imap.auth.plain.disable", "true");
        props.setProperty("mail.imap.socketFactory.class", mailAccountProperties.getMailSocketFactoryClass());
        props.setProperty("mail.imap.socketFactory.port", port);
        props.setProperty("mail.imaps.ssl.trust", "*");
        props.setProperty("mail.mime.encodefilename", "true");
        props.setProperty("mail.mime.decodefilename", "true");
        props.setProperty("mail.imap.partialfetch", partialFetch);
        props.setProperty("mail.imap.fetchsize", fetchSize);

        props.setProperty("mail.imap.timeout", mailAccountProperties.getTimeout());
        props.setProperty("mail.imap.connectiontimeout", mailAccountProperties.getTimeout());
        props.setProperty("mail.imap.writetimeout", mailAccountProperties.getTimeout());
        props.setProperty("mail.imap.connectionpooltimeout", mailAccountProperties.getTimeout());

        if(StringUtils.isNotEmpty(mailAccountProperties.getMailAuthMechanism()))
            props.setProperty("mail.imap.auth.mechanisms", mailAccountProperties.getMailAuthMechanism());

        if(StringUtils.isNotEmpty(mailAccountProperties.getMailNTLMDomain()))
            props.setProperty("mail.imap.auth.ntlm.domain", mailAccountProperties.getMailNTLMDomain());


        logger.info("[IMAP] Connecting to " + host + ':' + port + ", user=" + user);


        Session session = Session.getInstance(props, null);
        Store store = session.getStore(new URLName("imap://" + email));
        store.connect(host, user, decryptedPassword);

        logger.info("[IMAP] Connected to " + host + ':' + port + ", user=" + user);
        return store;
    }

    private String decryptPassword(String encryptedPassword) throws Exception
    {
        if(cipher == null) {
            SecretKeySpec key = new SecretKeySpec(mailKey.getBytes("UTF8"), "Blowfish");
            cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, key);
        }


        byte[] encryptedData = base64.decodeBase64(encryptedPassword);
        byte[] decrypted = cipher.doFinal(encryptedData);

        return new String(decrypted);
    }

    public static String encrypt(String Data)throws Exception{

        SecretKeySpec key = new SecretKeySpec(mailKey.getBytes("UTF8"), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return base64.encodeToString(cipher.doFinal(Data.getBytes("UTF8")));

    }

    private String getPrefixedProperty(String key,String prefix,  Properties properties)
    {
        return properties.getProperty(prefix+"."+key);
    }
}
