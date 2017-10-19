package generator.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.lang.model.element.Modifier;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

import generator.AppiumTestGenerator;
import generator.ExcelReader;
import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

public class AppiumTestGeneratorTest {

	private String path = "data/exceltemplate.xlsx";

	@Test
	public void test() throws FileNotFoundException, IOException {

		ExcelReader reader = new ExcelReader(path);

		reader.test();
		
		Map<String, Object> driverProperties = reader.getProperties();

		Map<String, Object> desiredCapabilities = reader.getDesiredCapabilities();

		
		AppiumTestGenerator generator = new AppiumTestGenerator();

		generator.addDesiredCapabilities(desiredCapabilities);
		generator.addDriverProperties(driverProperties);

		generator.generate();

	}
}
