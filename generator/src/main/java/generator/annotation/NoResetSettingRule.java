package generator.annotation;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class NoResetSettingRule implements TestRule {

	private Boolean noReset;

	@Override
	public Statement apply(Statement base, Description description) {

		NoResetSetting c = description.getAnnotation(NoResetSetting.class);
		if (c != null) {
			noReset = c.noReset();
		}
		return base;
	}

	public Boolean isNoReset() {
		return noReset;
	}
}
