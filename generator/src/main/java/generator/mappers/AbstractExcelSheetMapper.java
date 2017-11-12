package generator.mappers;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import generator.handlers.HandlerExecution;

public abstract class AbstractExcelSheetMapper<T> implements ExcelSheetMapper<T> {

	protected XSSFSheet sheet;

	public HandlerExecution<T> getHandler(XSSFSheet sheet) {
		this.sheet = sheet;

		XSSFRow typeRow = sheet.getRow(0);

		if (typeRow != null && getType().equals(sheet.getRow(0).getCell(0).getStringCellValue())) {
			return defaultHandle();
		}

		return null;
	}

	abstract protected HandlerExecution<T> defaultHandle();

	abstract protected String getType();
}
