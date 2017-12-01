package generator.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.collect.Iterators;

import models.AccountInfo;

/**
 * 讀取Excel測試資料Sheet轉成AccountInfo
 * 
 * @author Cyndi
 *
 */
public class AccountHandler implements HandlerExecution<List<AccountInfo>> {

	private List<AccountInfo> accounts = new ArrayList<>();

	private final XSSFSheet sheet;

	private String typeName;

	public AccountHandler(XSSFSheet sheet, String typeName) {
		this.sheet = sheet;
		this.typeName = typeName;
	}

	@Override
	public void addRecordTo(Map<String, Object> store) {

		List<AccountInfo> records = (List<AccountInfo>) store.get(getTypeName());

		if (records == null) {
			records = new ArrayList<>();
			records.addAll(accounts);

			store.put(getTypeName(), records);
		} else {
			records.addAll(accounts);
		}
	}

	@Override
	public void generate() {

		Iterator<Row> rowIterator = sheet.iterator();

		// 前進兩格，由第三列開始
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
	}

	@Override
	public List<AccountInfo> getData() {
		return accounts;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	/**
	 * 檢查測試資料前四格是否不為empty
	 * 
	 * @param row
	 * @return
	 */
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

	/**
	 * 檢查表格Title
	 * 
	 * @param row
	 * @return
	 */
	private boolean isTitleColumn(Row row) {
		return "使用者身份".equals(row.getCell(0).getStringCellValue());
	}

}
