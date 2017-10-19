package ExcelReader;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import generator.ExcelReader;

public class ExcelReaderTest {

	private String path = "data/exceltemplate.xlsx";

	@Test
	public void test() throws FileNotFoundException, IOException {
		ExcelReader reader = new ExcelReader(path);

		reader.test();
		reader.showData();
	}

}
