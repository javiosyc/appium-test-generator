package generator.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import generator.test.annotation.TestingAccount;

/**
 * UserLogin TestRule
 * 
 * @author Cyndi
 *
 */
public class UserLoginTestRule implements TestRule {

	private Boolean hasUser;

	private String password;
	private String pid;
	private String userName;

	@Override
	public Statement apply(Statement base, Description description) {

		TestingAccount testingAccount = description.getAnnotation(TestingAccount.class);
		if (testingAccount != null) {
			userName = testingAccount.userName();
			pid = testingAccount.pid();
			password = testingAccount.password();
			hasUser = true;
		} else {
			hasUser = false;
		}
		return base;
	}

	public Boolean getHasUser() {
		return hasUser;
	}

	public String getPassword() {
		return password;
	}

	public String getPid() {
		return pid;
	}

	public String getUserName() {
		return userName;
	}

	public void setHasUser(Boolean hasUser) {
		this.hasUser = hasUser;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
