package generator.utils;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import generator.AppiumTestGenerator;
import generator.ExcelReader;

public class AppiumTestGeneratorTest {

	private String path = "data/ScriptGenerator.xlsx";

	@Test
	public void test() throws FileNotFoundException, IOException {

		String outDir = System.getProperty("outdir");

		if (StringUtils.isBlank(outDir)) {
			outDir = "/Users/cyndi/git/appium-script/test/src/test/java";
		}

		ExcelReader reader = new ExcelReader(path);

		reader.read();

		AppiumTestGenerator generator = new AppiumTestGenerator(reader);

		generator.setOutputDir(outDir);

		generator.generate();

		generator.writeTo();
	}
}
