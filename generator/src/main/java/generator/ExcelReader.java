package generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.Iterators;
import com.google.common.primitives.Ints;

import generator.utils.DesiredCapabilityUtils;
import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;
import models.AccountInfo;
import models.Command;
import models.Feature;
import models.Scenario;
import models.Step;

public class ExcelReader {

	private Map<String, Object> desiredCapabilities = new HashMap<>();

	private Map<String, Object> properties = new HashMap<>();

	private List<Feature> features = new ArrayList<>();

	public List<Feature> getFeatures() {
		return features;
	}

	private List<AccountInfo> accounts = new ArrayList<>();

	private final String excelFile;

	private final XSSFWorkbook wb;

	private final int count;

	private List<String> gherkins = Arrays.asList("Given", "And", "When", "Then");

	public XSSFWorkbook getWb() {
		return wb;
	}

	public ExcelReader(String excelFile) throws FileNotFoundException, IOException {
		this.excelFile = excelFile;
		wb = new XSSFWorkbook(new FileInputStream(excelFile));
		count = wb.getNumberOfSheets();
	}

	public void test() throws IOException {

		IntStream.range(0, count).forEach((index) -> {
			parseSheet(wb.getSheetAt(index));
		});

		wb.close();
	}

	public void showData() {
		accounts.stream().forEach(System.out::println);

		for (Map.Entry<String, Object> entry : desiredCapabilities.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());

		}

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			System.out.println(entry.getKey() + ":" + entry.getValue());
		}

		features.stream().forEach(System.out::println);
	}

	private void parseSheet(XSSFSheet sheet) {

		Row titleRow = sheet.getRow(1);

		if (titleRow == null || titleRow.getCell(0) == null)
			return;

		String type = StringUtils.trim(titleRow.getCell(0).getStringCellValue());

		if ("settings".equals(type)) {

			if (sheet.getPhysicalNumberOfRows() < 2) {
				return;
			}

			Map<String, String> mapper = getCapabilityNameMapper();
			Iterator<Row> rowIterator = sheet.iterator();

			Iterators.advance(rowIterator, 1);

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				if (StringUtils.isBlank(row.getCell(0).getStringCellValue())) {
					continue;
				}

				if (isDesiredCapability(row)) {
					String column = row.getCell(0).getStringCellValue();

					String key = mapper.getOrDefault(column, column);

					if (row.getCell(1) == null)
						continue;

					CellType cell = row.getCell(1).getCellTypeEnum();

					Object value = null;

					switch (cell) {
					case BOOLEAN:
						value = row.getCell(1).getBooleanCellValue();
						break;

					case STRING:
						String cellValue = row.getCell(1).getStringCellValue();
						if (StringUtils.isNotBlank(cellValue))
							value = cellValue;
						break;
					default:
						break;
					}

					if (value != null)
						desiredCapabilities.put(key, value);
				}

				if (implicitlyWaitProperty(row)) {

					if (row.getCell(1) != null) {

						Double cellValue = row.getCell(1).getNumericCellValue();

						Integer waitSec = cellValue.intValue();

						properties.put("implicitlyWait", waitSec);
					}
				}
			}

		} else if ("data".equals(type)) {

			if (sheet.getPhysicalNumberOfRows() < 3) {
				return;
			}

			Iterator<Row> rowIterator = sheet.iterator();
			Iterators.advance(rowIterator, 2);

			while (rowIterator.hasNext()) {

				Row row = rowIterator.next();

				if (!checkAccountInfo(row))
					continue;

				if (isTitleColumn(row))
					continue;

				AccountInfo accountInfo = new AccountInfo();
				accountInfo.setType(row.getCell(0).getStringCellValue());
				accountInfo.setPid(row.getCell(1).getStringCellValue());
				accountInfo.setUserName(row.getCell(2).getStringCellValue());
				accountInfo.setPassword(row.getCell(3).getStringCellValue());
				accountInfo.setComment(row.getCell(4).getStringCellValue());

				accounts.add(accountInfo);
			}

		} else if ("script".equals(type)) {
			if (sheet.getPhysicalNumberOfRows() < 3) {
				return;
			}

			Row typeRow = sheet.getRow(1);

			String featureDesc = typeRow.getCell(1).getStringCellValue();
			String featureName = typeRow.getCell(2).getStringCellValue();
			String packageName = typeRow.getCell(3).getStringCellValue();

			Feature feature = new Feature();
			feature.setName(featureName);
			feature.setDesc(featureDesc);
			feature.setPackageName(packageName);
			feature.setScenarios(new ArrayList<>());

			features.add(feature);

			Iterator<Row> rowIterator = sheet.iterator();
			Iterators.advance(rowIterator, 2);

			Scenario currentScenario = null;

			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();

				String cellString = getStringCellValue(row, 0);

				if (StringUtils.isBlank(cellString)) {
					continue;
				}

				if (gherkins.contains(cellString)) {
					System.out.println("step");

					if (currentScenario != null) {
						String stepType = row.getCell(0).getStringCellValue();
						String desc = row.getCell(1).getStringCellValue();
						String commandType = row.getCell(2).getStringCellValue();

						Step step = new Step();
						step.setDesc(desc);
						step.setGherkinType(stepType);

						Command command = new Command();
						command.setType(commandType);

						step.setCommand(command);

						for (int cn = 3; cn < row.getLastCellNum(); cn++) {
							Cell cell = row.getCell(cn);
							CellType cellType = cell.getCellTypeEnum();
							switch (cellType) {
							case STRING:
								command.addParam(row.getCell(cn).getStringCellValue());
								break;
							case NUMERIC:
								command.addParam(row.getCell(cn).getNumericCellValue());
								break;
							default:
								break;
							}
						}
						System.out.println(step);

						currentScenario.getSteps().add(step);
					}

				} else {

					System.out.println("Scenario");

					String desc = row.getCell(0).getStringCellValue();
					String name = row.getCell(1).getStringCellValue();

					currentScenario = new Scenario();

					currentScenario.setName(name);
					currentScenario.setDesc(desc);
					currentScenario.setSteps(new ArrayList<Step>());

					feature.getScenarios().add(currentScenario);

				}

			}
		}
	}

	private String getStringCellValue(Row row, int i) {
		Cell cell = row.getCell(i);
		if (cell == null) {
			return "";
		}

		return cell.getStringCellValue();
	}

	private boolean implicitlyWaitProperty(Row row) {

		String title = row.getCell(0).getStringCellValue();

		return "尋找元素等待時間".equals(title);
	}

	private boolean isDesiredCapability(Row row) {

		String title = row.getCell(0).getStringCellValue();

		if (getCapabilityNameMapper().get(title) != null) {
			return true;
		}

		if (DesiredCapabilityUtils.isIOSMobileCapability(title)) {
			return true;
		}

		return false;
	}

	private Map<String, String> getCapabilityNameMapper() {
		Map<String, String> mapper = new HashMap<>();

		mapper.put("App路徑", MobileCapabilityType.APP);
		mapper.put("Appium版本", MobileCapabilityType.APPIUM_VERSION);

		mapper.put("手機選項", MobileCapabilityType.DEVICE_NAME);
		mapper.put("作業系統選項", MobileCapabilityType.PLATFORM_VERSION);

		return mapper;
	}

	private boolean isTitleColumn(Row row) {
		return "使用者身份".equals(row.getCell(0).getStringCellValue());
	}

	private boolean checkAccountInfo(Row row) {
		return !IntStream.range(0, 4).anyMatch((index) -> {
			Cell cell = row.getCell(index);
			String cellValue;
			if (cell == null) {
				return true;
			}
			cellValue = cell.getStringCellValue();
			return StringUtils.isEmpty(cellValue);
		});
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public String getExcelFile() {
		return excelFile;
	}

	public Map<String, Object> getDesiredCapabilities() {
		return desiredCapabilities;
	}

	public List<AccountInfo> getAccounts() {
		return accounts;
	}

	public int getCount() {
		return count;
	}

}
