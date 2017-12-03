package generator.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.collect.Iterators;

import generator.utils.ExcelUtils;
import models.Command;
import models.CommonMethod;
import models.CommonUtilClass;
import models.Step;

/**
 * 讀取Excel登入情境Sheet轉成CommonUtilClass
 * 
 * @author Cyndi
 *
 */
public class CommonStepHandler implements HandlerExecution<CommonUtilClass> {
	private static final String METHOD_COMMENT_TAG = "MethodComment";

	private static final String METHOD_NAME_TAG = "MethodName";

	private static final String METHOD_NOREST_TAG = "noReset";

	private final XSSFSheet sheet;
	private String typeName;
	private CommonUtilClass utilClass;

	public CommonStepHandler(XSSFSheet sheet, String typeName) {
		this.sheet = sheet;
		this.typeName = typeName;
		utilClass = new CommonUtilClass();
		utilClass.setMethods(new ArrayList<>());
	}

	@Override
	public void addRecordTo(Map<String, Object> store) {
		List<CommonUtilClass> records = (List<CommonUtilClass>) store.get(getTypeName());

		if (records == null) {
			records = new ArrayList<>();
			records.add(getData());
			store.put(getTypeName(), records);
		} else {
			records.add(getData());
		}

	}

	@Override
	public void generate() {

		int rowNumber = populateClassInfo(sheet);

		Iterator<Row> rowIterator = sheet.iterator();
		Iterators.advance(rowIterator, rowNumber);

		CommonMethod method = null;

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			String firstCell = getStringCellValue(row, 0);

			// 檢查第一欄名稱是否等於METHOD_NAME_TAG，若有則Create一個新的Method
			if (METHOD_NAME_TAG.equals(firstCell)) {
				String name = row.getCell(1).getStringCellValue();
				method = new CommonMethod();
				method.setName(name);
				method.setPackageName(utilClass.getPackageName());
				method.setClassName(utilClass.getName());
				method.setSteps(new ArrayList<>());

				utilClass.getMethods().add(method);

			} else if (METHOD_COMMENT_TAG.equals(firstCell)) {
				if (method != null) {
					String desc = row.getCell(1).getStringCellValue();
					method.setDesc(desc);

					Iterator<Cell> cellIterator = row.iterator();
					Iterators.advance(cellIterator, 2);

					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						String column = cell.getStringCellValue();

						if (METHOD_NOREST_TAG.equals(column)) {
							if (cellIterator.hasNext()) {
								Cell noResetCell = cellIterator.next();

								Optional<Boolean> noReset = ExcelUtils.getCellValueToBoolean(noResetCell);
								if (noReset.isPresent()) {
									method.setNoReset(noReset.get());
								} else {
									method.setNoReset(false);
								}
							}
						}
					}
				}
			} else if (StringUtils.isNotBlank(firstCell) && !StringUtils.equals(firstCell, "Step")) {

				String desc = row.getCell(0).getStringCellValue();
				Step step = new Step();
				step.setDesc(desc);

				String commandType = row.getCell(1).getStringCellValue();

				Command command = new Command();
				command.setType(commandType);

				// 將第三欄開始的內容都當為Command參數
				for (int cn = 2; cn < row.getLastCellNum(); cn++) {
					Cell cell = row.getCell(cn);

					Optional<Object> value = ExcelUtils.getCellValue(cell);

					if (value.isPresent()) {
						command.addParam(value.get());
					}
				}
				step.setCommand(command);

				method.getSteps().add(step);
			}

		}
	}

	@Override
	public CommonUtilClass getData() {
		return utilClass;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	/**
	 * 取第i格欄位(Cell)的值
	 * 
	 * @param row
	 * @param i
	 * @return
	 */
	private String getStringCellValue(Row row, int i) {
		Cell cell = row.getCell(i);
		if (cell == null) {
			return "";
		}

		return cell.getStringCellValue();
	}

	private int populateClassInfo(XSSFSheet sheet) {
		String className = sheet.getRow(1).getCell(1).getStringCellValue();
		String desc = sheet.getRow(2).getCell(1).getStringCellValue();
		String packageName = sheet.getRow(3).getCell(1).getStringCellValue();

		utilClass.setName(className);
		utilClass.setDesc(desc);
		utilClass.setPackageName(packageName);

		return 4;
	}

}
