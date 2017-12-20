package generator.test.rules;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

/**
 * 截圖例外處理
 * 
 * @author Cyndi
 *
 */
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
						// 拍照語法
						File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

						// com.esun.automation.iphone8.ios11_2.demoCommonUtils.InstructionManualTest
						// 取最後一個符號之前＝>com.esun.automation.iphone8.ios11_2.demoCommonUtils
						String packageName = StringUtils.substringBeforeLast(description.getClassName(), ".");

						// com.esun.automation.iphone8.ios11_2.demoCommonUtils
						// 取最後一個符號之後=>demoCommonUtils
						packageName = StringUtils.substringAfterLast(packageName, ".");

						// com.esun.automation.iphone8.ios11_2.demoCommonUtils.InstructionManualTest
						// 取最後一個符號之 後＝>InstructionManualTest
						String className = StringUtils.substringAfterLast(description.getClassName(), ".");

						// create folder
						Path path = Paths.get("img" + File.separator + packageName);

						if (!Files.exists(path)) {
							Files.createDirectories(path);
						}

						String methodName = description.getMethodName();

						DateFormat df = new SimpleDateFormat("MMddHHmm");
						Date now = new Date();
						String dateString = df.format(now);

						String fileName = MessageFormat.format("{0}-{1}-{2}.png", className, methodName, dateString);

						FileUtils.copyFile(srcFile, new File(path + File.separator + fileName));

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
