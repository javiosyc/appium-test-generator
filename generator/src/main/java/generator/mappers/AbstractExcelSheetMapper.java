package generator.mappers;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import generator.handlers.HandlerExecution;

/**
 * 實作ExcelSheetMapper抽象類別
 * 
 * @author Cyndi
 *
 * @param <T>
 */
public abstract class AbstractExcelSheetMapper<T> implements ExcelSheetMapper<T> {

	protected XSSFSheet sheet;

	/**
	 * 讀第一列第一格判斷Type
	 */
	@Override
	public HandlerExecution<T> getHandler(XSSFSheet sheet) {
		this.sheet = sheet;

		XSSFRow typeRow = sheet.getRow(0);

		if (typeRow != null && getType().equals(typeRow.getCell(0).getStringCellValue())) {
			return defaultHandle();
		}

		return null;
	}

	abstract protected HandlerExecution<T> defaultHandle();

	/**
	 * Excel中裝置資訊Sheet，判斷Type
	 * 
	 * @return
	 */
	abstract protected String getType();
}
