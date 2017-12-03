package generator.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.collect.Iterators;

import generator.utils.DesiredCapabilityUtils;
import generator.utils.ExcelUtils;
import io.appium.java_client.remote.MobileCapabilityType;

/**
 * 讀取Excel測試資料Sheet處理desiredCapabilities,driverProperties
 * 
 * @author Cyndi
 *
 */
public class SettingHandler implements HandlerExecution<Map<String, Map<String, Object>>> {

	private static final String DESIRED_CAPABILITIES = "desiredCapabilities";

	private static final String DRIVER_PROPERTIES = "driverProperties";

	private Map<String, Object> desiredCapabilities = new HashMap<>();

	private Map<String, Object> driverProperties = new HashMap<>();

	private Map<String, String> mapper;

	private Map<String, Map<String, Object>> properties = new HashMap<>();

	private final XSSFSheet sheet;

	private String typeName;

	public SettingHandler(XSSFSheet sheet, String typeName) {
		this.sheet = sheet;
		this.typeName = typeName;
		mapper = getCapabilityNameMapper();

		properties.put(DESIRED_CAPABILITIES, desiredCapabilities);
		properties.put(DRIVER_PROPERTIES, driverProperties);
	}

	@Override
	public void addRecordTo(Map<String, Object> store) {

		Map<String, Map<String, Object>> records = (Map<String, Map<String, Object>>) store.get(getTypeName());

		if (records == null) {
			store.put(getTypeName(), properties);

		} else {

			Map<String, Object> desiredCapabilities = records.get(DESIRED_CAPABILITIES);

			Map<String, Object> driverProperties = records.get(DRIVER_PROPERTIES);

			for (Map.Entry<String, Object> mapEntry : this.desiredCapabilities.entrySet()) {
				desiredCapabilities.put(mapEntry.getKey(), mapEntry.getValue());
			}

			for (Map.Entry<String, Object> mapEntry : this.driverProperties.entrySet()) {
				driverProperties.put(mapEntry.getKey(), mapEntry.getValue());
			}
		}
	}

	@Override
	public void generate() {

		Iterator<Row> rowIterator = sheet.iterator();

		// 前進一格，由第二列開始
		Iterators.advance(rowIterator, 1);

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			if (StringUtils.isBlank(row.getCell(0).getStringCellValue())) {
				continue;
			}

			if (isDesiredCapability(row)) {
				String column = row.getCell(0).getStringCellValue();

				String key = mapper.getOrDefault(column, column);

				Cell cell = row.getCell(1);
				if (cell == null)
					continue;

				Optional<Object> value = ExcelUtils.getCellValue(cell);

				if (value.isPresent()) {
					desiredCapabilities.put(key, value.get());
				}
			}

			if (implicitlyWaitProperty(row)) {

				Cell valueCell = row.getCell(1);

				if (valueCell != null) {

					Optional<Integer> waitSec = ExcelUtils.getCellValueToInteger(valueCell);

					if (waitSec.isPresent()) {
						driverProperties.put("implicitlyWait", waitSec.get());
					}
				}
			}
		}

	}

	@Override
	public Map<String, Map<String, Object>> getData() {

		return properties;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Excel中文設定欄位對應appium可讀取之Capabilities
	 * 
	 * @return
	 */
	private Map<String, String> getCapabilityNameMapper() {
		Map<String, String> mapper = new HashMap<>();

		mapper.put("App路徑", MobileCapabilityType.APP);
		mapper.put("Appium版本", MobileCapabilityType.APPIUM_VERSION);

		mapper.put("手機選項", MobileCapabilityType.DEVICE_NAME);
		mapper.put("作業系統選項", MobileCapabilityType.PLATFORM_VERSION);

		return mapper;
	}

	private boolean implicitlyWaitProperty(Row row) {

		String title = row.getCell(0).getStringCellValue();

		return "尋找元素等待時間".equals(title);
	}

	private boolean isDesiredCapability(Row row) {

		String title = row.getCell(0).getStringCellValue();

		if (mapper.get(title) != null) {
			return true;
		}

		if (DesiredCapabilityUtils.isIOSMobileCapability(title)) {
			return true;
		}

		return false;
	}
}
