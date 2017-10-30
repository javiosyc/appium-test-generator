package generator.utils;

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
import models.Feature;

public class AppiumTestGeneratorTest {

	private String path = "data/exceltemplate.xlsx";

	@Test
	public void test() throws FileNotFoundException, IOException {

		String outDir = System.getProperty("outdir");

		if (StringUtils.isBlank(outDir)) {
			throw new RuntimeException("need to set outdir");
		}

		ExcelReader reader = new ExcelReader(path);

		reader.test();

		AppiumTestGenerator generator = new AppiumTestGenerator(reader);

		generator.setOutputDir(outDir);

		generator.generate();

		generator.writeTo();
	}
}
