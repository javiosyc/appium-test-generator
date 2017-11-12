package generator.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.collect.Iterators;

import generator.utils.DesiredCapabilityUtils;
import io.appium.java_client.remote.MobileCapabilityType;

public class SettingHandler implements HandlerExecution<Map<String, Map<String, Object>>> {

	private final XSSFSheet sheet;

	private Map<String, Object> desiredCapabilities = new HashMap<>();

	private Map<String, Map<String, Object>> properties = new HashMap<>();

	private Map<String, Object> driverProperties = new HashMap<>();

	public SettingHandler(XSSFSheet sheet) {
		this.sheet = sheet;
		properties.put("desiredCapabilities", desiredCapabilities);
		properties.put("driverProperties", driverProperties);
	}

	@Override
	public void generate() {

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

					driverProperties.put("implicitlyWait", waitSec);
				}
			}
		}

	}

	@Override
	public String getName() {
		return "settings";
	}

	@Override
	public void addRecordTo(Map<String, Object> store) {

		Map<String, Map<String, Object>> records = (Map<String, Map<String, Object>>) store.get(getName());

		if (records == null) {
			store.put(getName(), properties);

		} else {

			Map<String, Object> desiredCapabilities = records.get("desiredCapabilities");

			Map<String, Object> driverProperties = records.get("driverProperties");

			for (Map.Entry<String, Object> mapEntry : this.desiredCapabilities.entrySet()) {
				desiredCapabilities.put(mapEntry.getKey(), mapEntry.getValue());
			}

			for (Map.Entry<String, Object> mapEntry : this.driverProperties.entrySet()) {
				driverProperties.put(mapEntry.getKey(), mapEntry.getValue());
			}
		}
	}

	@Override
	public List<Map<String, Map<String, Object>>> getDataFrom(Map<String, Object> store) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Map<String, Object>> getData() {

		return properties;
	}

	private Map<String, String> getCapabilityNameMapper() {
		Map<String, String> mapper = new HashMap<>();

		mapper.put("App路徑", MobileCapabilityType.APP);
		mapper.put("Appium版本", MobileCapabilityType.APPIUM_VERSION);

		mapper.put("手機選項", MobileCapabilityType.DEVICE_NAME);
		mapper.put("作業系統選項", MobileCapabilityType.PLATFORM_VERSION);

		return mapper;
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

	private boolean implicitlyWaitProperty(Row row) {

		String title = row.getCell(0).getStringCellValue();

		return "尋找元素等待時間".equals(title);
	}
}
