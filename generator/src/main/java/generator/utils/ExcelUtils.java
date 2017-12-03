package generator.utils;

import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class ExcelUtils {

	public static Optional<Object> getCellValue(Cell cell) {

		CellType cellType = cell.getCellTypeEnum();

		Object value = null;

		switch (cellType) {
		case STRING:
			value = cell.getStringCellValue();
			break;
		case NUMERIC:
			value = cell.getNumericCellValue();
			break;
		case BOOLEAN:
			value = cell.getBooleanCellValue();
		default:
			break;
		}
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public static Optional<Boolean> getCellValueToBoolean(Cell cell) {

		CellType cellType = cell.getCellTypeEnum();

		Boolean value = null;

		switch (cellType) {
		case STRING:
			value = Boolean.valueOf(cell.getStringCellValue());
			break;
		case BOOLEAN:
			value = cell.getBooleanCellValue();
			break;
		default:
			break;
		}
		return value == null ? Optional.empty() : Optional.of(value);
	}

	public static Optional<Integer> getCellValueToInteger(Cell cell) {

		CellType cellType = cell.getCellTypeEnum();

		Integer value = null;

		switch (cellType) {
		case STRING:
			value = Integer.valueOf(cell.getStringCellValue());
			break;
		case NUMERIC:
			value = ((Double) cell.getNumericCellValue()).intValue();
			break;
		default:
			break;
		}
		return value == null ? Optional.empty() : Optional.of(value);
	}
}
