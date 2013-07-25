package pl.net.bluesoft.rnd.processtool.usersource;

import java.util.List;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.exception.UserSourceException;

/**
 * User source interface 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public interface IUserSource 
{
	/** 
	 * Get {@link UserData} by provided login 
	 * 
	 * @param login user login in Aperte
	 * @return {@link UserData} instance
	 * @throws UserSourceException if no user with given login is found
	 */
	UserData getUserByLogin(String login) throws UserSourceException;

	/**
	 * Get {@link UserData} by provided login for given comapnyId
	 * 
	 * @param login user login in Aperte
	 * @param companyId id of the company
	 * @return {@link UserData} instance
	 * @throws UserSourceException if no user with given login is found in this comapnyId
	 */
	UserData getUserByLogin(String login, Long companyId) throws UserSourceException;

	/**
	 * Get {@link UserData} by provided e-mail address for given comapnyId
	 * 
	 * @param email user e-mail address
	 * @param companyId id of the company
	 * @return {@link UserData} instance
	 * @throws UserSourceException if no user with given login is found in this comapnyId
	 */
	UserData getUserByEmail(String email);

	/**
	 * Get all users
	 * 
	 * @return
	 */
	List<UserData> getAllUsers();

}
