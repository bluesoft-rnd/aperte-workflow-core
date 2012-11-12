package org.aperteworkflow.util.ldap;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.aperteworkflow.util.liferay.LiferayBridge;
import org.aperteworkflow.util.liferay.PortalBridge;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.util.lang.Strings;

import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropertiesUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

/**
 * @author amichalak@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class LdapBridge {
    private static final Logger logger = Logger.getLogger(LdapBridge.class.getName());

    private static final String[] AUDIT_ATTRIBUTE_IDS = {"creatorsName", "createTimestamp", "modifiersName", "modifyTimestamp"};
    public static final String LDAP_CUSTOM_USER_ATTRIBUTES = "ldap.user.attributes";

    public static boolean isLdapActive(long companyId) {
        try {
            return PortalBridge.getBoolean(companyId, PropsKeys.LDAP_AUTH_ENABLED, false);
        }
        catch (Exception e) {
            throw new LiferayBridge.LiferayBridgeException(e);
        }
    }

    public static String getPropertyPostfix(long ldapServerId) {
        return ldapServerId > 0 ? "." + ldapServerId : "";
    }

    private static Properties loadUserAttributesProperty(ProcessToolContext ctx) {
        if (ctx == null) {
            ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        }
        if (ctx == null) {
            throw new LiferayBridge.LiferayBridgeException("Unable to find ProcessToolContext in thread local");
        }
        String properties = ctx.getSetting(LDAP_CUSTOM_USER_ATTRIBUTES);
        if (properties == null || !Strings.hasText(properties)) {
            return null;
        }
        properties = properties.replace(';', '\n');
        try {
            return PropertiesUtil.load(properties);
        }
        catch (IOException e) {
            throw new LiferayBridge.LiferayBridgeException(e);
        }
    }

    public static Map<String, Properties> getLdapUserAttributes(UserData user, ProcessToolContext ctx) {
        return getLdapUsersAttributes(Collections.singleton(user), ctx);
    }
    
    public static Map<String, Properties> getLdapUserAttributes(UserData user, Properties ldapUserAttributes, ProcessToolContext ctx) {
        return getLdapUsersAttributes(Collections.singleton(user), ldapUserAttributes, ctx);
    }
        
    public static Map<String, Properties> getLdapUsersAttributes(Collection<UserData> users, ProcessToolContext ctx) {
        return getLdapUsersAttributes(users, null, ctx);
    }

    public static Map<String, Properties> getLdapUsersAttributes(Collection<UserData> users, Properties ldapUserAttributes, ProcessToolContext ctx) {
        if (ldapUserAttributes == null || ldapUserAttributes.isEmpty()) {
            ldapUserAttributes = loadUserAttributesProperty(ctx);
        }
        
        logger.info("ldapUserAttributes are: "+ldapUserAttributes);
        
        Map<Long, Set<Long>> ldapServerMappings = new HashMap<Long, Set<Long>>();
        Map<String, Properties> result = new HashMap<String, Properties>();
        for (UserData user : users) {
            Properties properties = new Properties();
            result.put(user.getLogin(), properties);
            if (user.getCompanyId() != null) {
                Set<Long> ids = ldapServerMappings.get(user.getCompanyId());
                if (ids == null) {
                    ids = new HashSet<Long>();
                    ldapServerMappings.put(user.getCompanyId(), ids);
                }
                try {
                    ids.addAll(getLdapServerIds(user.getCompanyId()));
                }
                catch (Exception e) {
                    throw new LiferayBridge.LiferayBridgeException(e);
                }
            }
        }
        if (!ldapServerMappings.isEmpty() && ldapUserAttributes != null && !ldapUserAttributes.isEmpty()) {
			for (Map.Entry<Long, Set<Long>> entry : ldapServerMappings.entrySet()) {
				Long companyId = entry.getKey();
                Set<Long> ids = entry.getValue();
                for (Long ldapServerId : ids) {
                    LdapContext context;
                    try {
                        context = getContext(ldapServerId, companyId);
                    }
                    catch (Exception e) {
                        throw new LiferayBridge.LiferayBridgeException(e);
                    }
//                    if (context == null) {
//                        logger.warning("Could not initialize LDAP context: [ldapServerId, companyId] = [" + ldapServerId + ", " + companyId + "]");
//                    }
//                    else {
                        try {
                            updateUserPropertiesMap(result, context, ldapServerId, companyId, ldapUserAttributes);
                        }
                        catch (Exception e) {
                            throw new LiferayBridge.LiferayBridgeException(e);
                        }
                        finally {
                            try {
                                context.close();
                            }
                            catch (NamingException e) {
                                throw new LiferayBridge.LiferayBridgeException(e);
                            }
                        }
//                    }
                }
            }
        }

        return result;
    }

    private static void updateUserPropertiesMap(Map<String, Properties> propertiesMap, LdapContext context,
                                                Long ldapServerId, Long companyId, final Properties customAttributes) throws Exception {
        Properties userMappings = getUserMappings(ldapServerId, companyId);
        final String userMappingsScreenName = GetterUtil.getString(userMappings.getProperty("screenName")).toLowerCase();
        Set<String> attributesToFetch = new HashSet<String>() {{
            add(userMappingsScreenName);
            for (String sn : customAttributes.stringPropertyNames()) {
                add(customAttributes.getProperty(sn));
            }
        }};
        
        String screenName = propertiesMap.size() == 1 ? propertiesMap.entrySet().iterator().next().getKey() : null;
        

        /* User paged results to control page size. Some systems can have limited query size (for example Active Directory, which limits 
         * results count to 1000. So we must use Range property to perform multiple search
         */
		PagedResultsControl pagedControls = new PagedResultsControl(500, Control.CRITICAL);
		context.setRequestControls(new Control[] {pagedControls});
		
		/* Cookie is used to inform server to send another page */
		byte[] cookie = null;
	    int total = 0;

        
        /* Sum of all users imported from LDAP */
        int ldapUserCount = 0;
        
        
        do
        {
        	/* Create search controls and apply range limit */
        	SearchControls searchControls = createSearchControls(screenName, attributesToFetch);
        	NamingEnumeration<SearchResult> enu = searchLdapUsers(context, companyId, ldapServerId, screenName, searchControls);
        	
        	/* There should be no exception here becouse we use paged searching */
            while (enu.hasMore()) 
            {
            	ldapUserCount++;
                SearchResult result = enu.nextElement();
                Attributes attributes = result.getAttributes();
                String login = getAttributeValue(attributes, userMappingsScreenName, null);

                if (login != null) 
                {
                	logger.info("Teta user login: "+login);
                    Properties userAttributes = propertiesMap.get(login);
                    if (userAttributes != null) {
                        for (String propertyName : customAttributes.stringPropertyNames()) {
                            String value = getAttributeValue(attributes, customAttributes.getProperty(propertyName), null);
                            logger.info("Teta user property="+propertyName+", value="+value);
                            if (value != null) {
                                userAttributes.setProperty(propertyName, value);
                            }
                        }
                    }
                    else
                    {
                    	 logger.info("Teta user userAttributes are empty for login="+login);
                    }

                }
            }
            
            /* Search response controls form paged results control */
            Control[] controls = context.getResponseControls();
            if (controls != null) 
            {
                for (int i = 0; i < controls.length; i++) 
                {
                    if (controls[i] instanceof PagedResultsResponseControl) 
                    {
                        PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[i];
                        total = prrc.getResultSize();
                        cookie = prrc.getCookie();
                        
                        /* Update ldap context. In this moment, we inform server that it should
                         * send another page of results
                         * 
                         * If cookie == null, there is no more results
                         */
                        pagedControls = new PagedResultsControl(500, cookie, Control.CRITICAL);
                		context.setRequestControls(new Control[] {pagedControls});
                    }
                }
            }
	            
        	
        	enu.close();
        	
        /* No more results, end of ldap synchronization */
        } while(cookie != null);
        

        logger.info("LDAP users forund: "+ldapUserCount);
    }
    
    
    private static SearchControls createSearchControls(String userLogin, Set<String> attributesToFetch)
    {
    	int count = Strings.hasText(userLogin) ? 1 : 0;

        SearchControls searchControls = new SearchControls(SearchControls.SUBTREE_SCOPE, count, 0,
                attributesToFetch.toArray(new String[attributesToFetch.size()]), false, false);
        
        return searchControls;
    }

    private static NamingEnumeration<SearchResult> searchLdapUsers(LdapContext context, Long companyId, Long ldapServerId, String userLogin,
    		SearchControls searchControls) throws Exception 
    		{
        String screenName = Strings.hasText(userLogin) ? userLogin : StringPool.STAR;
        String postfix = getPropertyPostfix(ldapServerId);
        String baseDN = PortalBridge.getString(companyId, PropsKeys.LDAP_BASE_DN + postfix);
        String filter = getAuthSearchFilter(ldapServerId, companyId, StringPool.BLANK, screenName, StringPool.BLANK);
        return context.search(baseDN, filter, searchControls);
    }
    
    public static String getAttributeValue(Attributes attributes, String id, String defaultValue) throws NamingException {
        Attribute attribute = attributes.get(id);
        Object obj = attribute != null ? attribute.get() : null;
        return obj != null ? obj.toString() : defaultValue;
    }

    public static LdapContext getContext(long ldapServerId, long companyId) throws Exception {
        String postfix = getPropertyPostfix(ldapServerId);
        String baseProviderURL = PortalBridge.getString(companyId, PropsKeys.LDAP_BASE_PROVIDER_URL + postfix);
        String pricipal = PortalBridge.getString(companyId, PropsKeys.LDAP_SECURITY_PRINCIPAL + postfix);
        String credentials = PortalBridge.getString(companyId, PropsKeys.LDAP_SECURITY_CREDENTIALS + postfix);
        return getContext(companyId, baseProviderURL, pricipal, credentials);
    }

    public static LdapContext getContext(long companyId, String providerURL, String principal, String credentials) throws Exception {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY, PortalBridge.getString(companyId, PropsKeys.LDAP_FACTORY_INITIAL));
        env.put(Context.PROVIDER_URL, providerURL);
        env.put(Context.SECURITY_PRINCIPAL, principal);
        env.put(Context.SECURITY_CREDENTIALS, credentials);
        env.put(Context.REFERRAL, PortalBridge.getString(companyId, PropsKeys.LDAP_REFERRAL));
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.maxsize", "50");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "10000");

        try {
            return new InitialLdapContext(env, null);
        }
        catch (Exception e) {
            throw new LiferayBridge.LiferayBridgeException(e);
        }
    }

    public static long getLdapServerId(long companyId, String screenName) throws Exception {
        long[] ldapServerIds = StringUtil.split(PortalBridge.getString(companyId, "ldap.server.ids"), 0L);
        for (long ldapServerId : ldapServerIds) {
            if (getUser(ldapServerId, companyId, screenName) != null) {
                return ldapServerId;
            }
        }

        if (ldapServerIds.length > 0) {
            return ldapServerIds[0];
        }

        return 0;
    }

    public static Set<Long> getLdapServerIds(Long companyId) throws Exception {
        Set<Long> ldapServerIds = new HashSet<Long>();
        long[] ids = StringUtil.split(PortalBridge.getString(companyId, "ldap.server.ids"), 0L);
        for (long id : ids) {
            ldapServerIds.add(id);
        }
        return ldapServerIds;
    }

    public static Binding getUser(long ldapServerId, long companyId, String screenName) throws Exception {
        String postfix = getPropertyPostfix(ldapServerId);
        LdapContext ldapContext = getContext(ldapServerId, companyId);
//        if (ldapContext == null) {
//            return null;
//        }

        NamingEnumeration<SearchResult> enu = null;
        try {
            String baseDN = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_BASE_DN + postfix);
            Properties userMappings = getUserMappings(ldapServerId, companyId);
            StringBundler filter = new StringBundler(5)
                    .append(StringPool.OPEN_PARENTHESIS)
                    .append(userMappings.getProperty("screenName"))
                    .append(StringPool.EQUAL)
                    .append(screenName)
                    .append(StringPool.CLOSE_PARENTHESIS);

            SearchControls searchControls = new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 0, null, false, false);
            enu = ldapContext.search(baseDN, filter.toString(), searchControls);
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (ldapContext != null) {
                ldapContext.close();
            }
        }

        if (enu.hasMoreElements()) {
            Binding binding = enu.nextElement();
            enu.close();
            return binding;
        }
        else {
            return null;
        }
    }

    public static Attributes getUserAttributes(LdapContext ldapContext, String fullDistinguishedName, Properties ldapUserAttributes) throws Exception {
        Attributes attributes = null;
        if (!ldapUserAttributes.isEmpty()) {
            String[] attributeIds = ArrayUtil.toStringArray(ldapUserAttributes.values().toArray(new Object[ldapUserAttributes.size()]));
            Name fullDN = new CompositeName().add(fullDistinguishedName);
            int attributeCount = attributeIds.length + AUDIT_ATTRIBUTE_IDS.length;
            String[] allAttributeIds = new String[attributeCount];
            System.arraycopy(attributeIds, 0, allAttributeIds, 0, attributeIds.length);
            System.arraycopy(AUDIT_ATTRIBUTE_IDS, 0, allAttributeIds, attributeIds.length, AUDIT_ATTRIBUTE_IDS.length);
            attributes = ldapContext.getAttributes(fullDN, allAttributeIds);
        }
        return attributes;
    }

    public static String getNameInNamespace(long ldapServerId, long companyId, Binding binding) throws Exception {
        String postfix = getPropertyPostfix(ldapServerId);
        String baseDN = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_BASE_DN + postfix);
        String name = binding.getName();
        if (name.startsWith(StringPool.QUOTE) && name.endsWith(StringPool.QUOTE)) {
            name = name.substring(1, name.length() - 1);
        }
        return Validator.isNull(baseDN) ? name.toString() : name.concat(StringPool.COMMA).concat(baseDN);
    }

    public static Properties getUserMappings(long ldapServerId, long companyId) throws Exception {
        String postfix = getPropertyPostfix(ldapServerId);
        Properties userMappings = PropertiesUtil.load(PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_USER_MAPPINGS + postfix));
        return userMappings;
    }

    public static String getAuthSearchFilter(long ldapServerId, long companyId, String emailAddress, String screenName, String userId) throws Exception {
        String postfix = getPropertyPostfix(ldapServerId);
        String filter = PrefsPropsUtil.getString(companyId, PropsKeys.LDAP_AUTH_SEARCH_FILTER + postfix);
        filter = StringUtil.replace(filter,
                new String[] {"@company_id@", "@email_address@", "@screen_name@", "@user_id@"},
                new String[] {String.valueOf(companyId), emailAddress, screenName, userId});
        return filter;
    }
}
