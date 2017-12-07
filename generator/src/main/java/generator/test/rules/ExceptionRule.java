package generator.test.rules;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

public class ExceptionRule implements TestRule {

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
						Calendar now = Calendar.getInstance();

						File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

						String packageName = StringUtils.substringBeforeLast(description.getClassName(), ".");

						packageName = StringUtils.substringAfterLast(packageName, ".");

						String className = StringUtils.substringAfterLast(description.getClassName(), ".");

						Path path = Paths.get("img/" + packageName);

						if (!Files.exists(path)) {
							Files.createDirectories(path);
						}
						String methodName = description.getMethodName();

						String fileName = MessageFormat.format("{0}-{1}-{2}:{3}.png", className, methodName,
								now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));

						FileUtils.copyFile(srcFile, new File(path + "/" + fileName));

					} catch (Exception ex) {
						System.out.println(ex);
						System.out.println("TakesScreenshot failed");
					}
				}
				throw e;
			}
		}
	}

	private IOSDriver<MobileElement> driver;

	public ExceptionRule() {
	}

	@Override
	public Statement apply(Statement base, Description description) {

		return new BusinessExceptionStatement(base, description);
	}

	public void setDriver(IOSDriver<MobileElement> driver) {
		this.driver = driver;
	}
}
