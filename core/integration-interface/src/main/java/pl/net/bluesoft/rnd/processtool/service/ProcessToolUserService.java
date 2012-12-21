package pl.net.bluesoft.rnd.processtool.service;

import org.hibernate.criterion.Criterion;
import pl.net.bluesoft.rnd.processtool.hibernate.CriteriaConfigurer;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ProcessToolUserService {
    UserData findUserByLogin(String login);

    UserData findUserByEmail(String email);

    UserData findUserById(Long id);

    List<UserData> findUsersByLogins(Collection<String> logins);

    List<UserData> findUsersByEmails(Collection<String> emails);

    List<UserData> findUsersByIds(Collection<Long> ids);

    List<UserData> findUsersByAttribute(String key, String... value);

    List<UserData> findUsersByAttributes(Map<String, String> attributeValues);

    List<UserData> findUsersContainingAttributes(String... keys);

    List<UserData> findUsersByExample(UserData userData);

    List<UserData> findAllUsers();

    List<UserData> findUsersByCriteria(CriteriaConfigurer configurer);

    List<UserData> findUsersByCriteria(Criterion... criteria);

    List<UserData> findUsersByCriteria(Collection<Criterion> criteria);

    UserData updateUser(UserData user);

    List<UserData> updateUsers(Collection<UserData> users);
}
