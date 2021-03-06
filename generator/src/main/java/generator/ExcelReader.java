package generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import generator.handlers.HandlerExecution;
import generator.mappers.AccountMapper;
import generator.mappers.CommonStepMapper;
import generator.mappers.ExcelSheetMapper;
import generator.mappers.ScriptMapper;
import generator.mappers.SettingMapper;
import models.AccountInfo;
import models.CommonUtilClass;
import models.Feature;

/**
 * 讀取Excel各分頁欄位
 * 
 * @author Cyndi
 *
 */
public class ExcelReader {

	private List<AccountInfo> accounts = new ArrayList<>();

	private final int count;

	private Map<String, Object> data = new HashMap<>();

	private Map<String, Object> desiredCapabilities = new HashMap<>();

	private final String excelFile;

	private List<Feature> features = new ArrayList<>();

	private List<ExcelSheetMapper<?>> mappers = new ArrayList<>();

	private Map<String, Object> properties = new HashMap<>();

	private final XSSFWorkbook wb;

	/**
	 * 
	 * @param excelFile檔案路徑
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public ExcelReader(String excelFile) throws FileNotFoundException, IOException {
		this.excelFile = excelFile;
		wb = new XSSFWorkbook(new FileInputStream(excelFile));
		count = wb.getNumberOfSheets();

		mappers.add(new ScriptMapper());
		mappers.add(new SettingMapper());
		mappers.add(new AccountMapper());
		mappers.add(new CommonStepMapper());
	}

	public List<AccountInfo> getAccounts() {
		return accounts;
	}

	public int getCount() {
		return count;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Map<String, Object> getDesiredCapabilities() {
		return desiredCapabilities;
	}

	public String getExcelFile() {
		return excelFile;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public XSSFWorkbook getWb() {
		return wb;
	}

	/**
	 * 開始讀取Excel
	 * 
	 * @throws IOException
	 */
	public void read() throws IOException {
		IntStream.range(0, count).forEach((index) -> {
			parseSheet(wb.getSheetAt(index));
		});

		wb.close();
	}

	/**
	 * 印出Excel內容
	 */
	public void showData() {

		for (Map.Entry<String, Object> entry : data.entrySet()) {
			Object values = entry.getValue();

			if (values instanceof ArrayList) {

				List list = (ArrayList) values;
				if (!list.isEmpty()) {
					if (list.get(0) instanceof Feature) {
						for (Object value : list) {
							Feature f = (Feature) value;
							System.out.println(f.getScenarios().size() + "---");

							f.getScenarios().forEach((s) -> {
								System.out.println(s.getName() + "-" + s.getDesc());

								s.getSteps().forEach(System.out::println);
							});
						}
					}
					if (list.get(0) instanceof CommonUtilClass) {
						for (Object value : list) {
							CommonUtilClass utilClass = (CommonUtilClass) value;
							System.out.println(
									utilClass.getName() + "-" + utilClass.getDesc() + "-" + utilClass.getPackageName());

							utilClass.getMethods().forEach(utilMethod -> {
								System.out.println("\t" + utilMethod.getName() + "-" + utilMethod.getDesc()
										+ "-noReset:" + utilMethod.isNoReset());

								utilMethod.getSteps().forEach((step) -> {
									System.out.print("\t\t" + step.getDesc() + step.getCommand().getType());

									step.getCommand().getParams().forEach((param) -> {
										System.out.print(" " + param);
									});
									System.out.println();
								});
							});
						}
					}

					if (list.get(0) instanceof AccountInfo) {

						List<AccountInfo> accounts = list;

						accounts.forEach((acc) -> {
							System.out.println(acc.getType() + "-" + acc.getUserName() + ":" + acc.getPid() + ":"
									+ acc.getPassword() + acc.getComment());
						});

					}
				}
			}

			if (values instanceof HashMap) {

				Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) values;

				if (!map.isEmpty()) {

					Map<String, Object> desiredCapabilities = map.get("desiredCapabilities");

					Map<String, Object> driverProperties = map.get("driverProperties");

					System.out.println("desiredCapabilities");
					for (Map.Entry<String, Object> mapEntry : desiredCapabilities.entrySet()) {
						System.out.println(mapEntry.getKey() + ":" + mapEntry.getValue());
					}

					System.out.println("driverProperties");
					for (Map.Entry<String, Object> mapEntry : driverProperties.entrySet()) {
						System.out.println(mapEntry.getKey() + ":" + mapEntry.getValue());
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param sheet
	 * @return
	 */
	private HandlerExecution<?> getHandler(XSSFSheet sheet) {
		for (ExcelSheetMapper<?> mapper : mappers) {
			HandlerExecution<?> handle = mapper.getHandler(sheet);
			if (handle != null)
				return handle;
		}
		return null;
	}

	/**
	 * 
	 * @param sheet
	 */
	private void parseSheet(XSSFSheet sheet) {

		HandlerExecution<?> execution = getHandler(sheet);

		if (execution == null) {
			return;
		}

		execution.generate();

		execution.addRecordTo(data);
	}

}
