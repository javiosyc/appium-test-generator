package generator.mappers;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import generator.handlers.HandlerExecution;

public interface ExcelSheetMapper<T>{

	public HandlerExecution<T> getHandler(XSSFSheet sheep);
}
