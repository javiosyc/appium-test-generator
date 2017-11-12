package sauce_appium_junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.lang.model.element.Modifier;
import javax.management.RuntimeErrorException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

import generator.AppiumTestGenerator;
import generator.ExcelReader;
import generator.annotation.Cyndi78;
import generator.annotation.Cyndi9478;
import generator.annotation.CyndiTestRule;
import generator.annotation.CyndiTestRule78;
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import models.Feature;

public class AppiumTestGeneratorTest {

	@Rule
	public CyndiTestRule rule = new CyndiTestRule();

	@Before
	public void a() {

		if (rule.isNoReset()) {
			System.out.println("cyndi");
		} else {
			System.out.println("cyndi7788");
		}
	}

	@Test
	@Cyndi9478(noReset = false)
	public void test() throws FileNotFoundException, IOException {

		System.out.println("---end===");
	}
}
