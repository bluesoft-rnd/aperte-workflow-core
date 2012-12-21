package pl.net.bluesoft.rnd.pt.ext.user.service;

import pl.net.bluesoft.rnd.processtool.model.UserAttribute;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class UserConverterUtils {
    public static UserData mergeUsers(UserData baseUser, UserData importedUser) {
        if (baseUser != importedUser) {
            baseUser.setLogin(importedUser.getLogin());
            baseUser.setCompanyId(importedUser.getCompanyId());
            baseUser.setEmail(importedUser.getEmail());
            baseUser.setJobTitle(importedUser.getJobTitle());
            baseUser.setFirstName(importedUser.getFirstName());
            baseUser.setLastName(importedUser.getLastName());
            baseUser.setLiferayUserId(importedUser.getLiferayUserId());
            baseUser.setRoleNames(importedUser.getRoleNames());
        }
        return mergeUserAttributes(baseUser, importedUser);
    }

    public static UserData mergeUserAttributes(UserData baseUser, UserData importedUser) {
        if (baseUser != importedUser) {
            for (UserAttribute a : importedUser.getMainAttributes()) {
                baseUser.setAttribute(a);
            }
        }
        return baseUser;
    }
}
