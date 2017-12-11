package generator.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import generator.test.annotation.NoResetSetting;

/**
 * NoResetSettingRule TestRule
 * 
 * @author Cyndi
 *
 */
public class NoResetSettingRule implements TestRule {

	private Boolean noReset;

	@Override
	public Statement apply(Statement base, Description description) {

		NoResetSetting noResetSetting = description.getAnnotation(NoResetSetting.class);
		if (noResetSetting != null) {
			noReset = noResetSetting.noReset();
		}
		return base;
	}

	public Boolean isNoReset() {
		return noReset;
	}
}
