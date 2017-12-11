package generator.test.utils;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Utils
 * 
 * @author Cyndi
 *
 */
public class CommandUtils {

	/**
	 * CheckAlert Utils,在Waiting秒數內有找到元素會按click；沒找到不會throw Exception。
	 * 
	 * @param driver
	 * @param waitSeconds
	 * @param name
	 * @param implicitlyWaitSeconds
	 */
	public static void presenceClick(WebDriver driver, long waitSeconds, String name, long implicitlyWaitSeconds) {

		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		try {
			(new WebDriverWait(driver, waitSeconds)).until(ExpectedConditions.presenceOfElementLocated(By.name(name)))
					.click();
		} catch (TimeoutException ex) {

		} finally {
			driver.manage().timeouts().implicitlyWait(implicitlyWaitSeconds, TimeUnit.SECONDS);
		}
	}
}
