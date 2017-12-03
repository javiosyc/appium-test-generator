package generator.test.rules;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

public class ExceptionRule implements TestRule {

	private IOSDriver<MobileElement> driver;

	public ExceptionRule() {
	}

	public void setDriver(IOSDriver<MobileElement> driver) {
		this.driver = driver;
	}

	@Override
	public Statement apply(Statement base, Description description) {

		return new BusinessExceptionStatement(base, description);
	}

	class BusinessExceptionStatement extends Statement {

		private final Statement base;

		private final Description description;

		public BusinessExceptionStatement(Statement base, Description description) {
			this.base = base;
			this.description = description;
		}

		@Override
		public void evaluate() throws Throwable {

			try {
				base.evaluate();
			} catch (Throwable e) {

				if (driver != null) {
					try {
						File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

						String fileName = description.getDisplayName();

						FileUtils.copyFile(srcFile, new File(String.valueOf(fileName) + ".png"));

					} catch (Exception ex) {
						System.out.println("TakesScreenshot failed");
					}
				}
				throw e;
			}
		}
	}
}
