package generator.handlers;

import java.util.ArrayList;
import java.util.HashMap;
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

public class AccountHandler implements HandlerExecution<List<AccountInfo>> {

	private final XSSFSheet sheet;

	private List<AccountInfo> accounts = new ArrayList<>();

	private Map<String, Map<String, Object>> properties = new HashMap<>();

	public AccountHandler(XSSFSheet sheet) {
		this.sheet = sheet;
	}

	@Override
	public void generate() {

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
	}

	@Override
	public String getName() {
		return "account";
	}

	@Override
	public void addRecordTo(Map<String, Object> store) {

		List<AccountInfo> records = (List<AccountInfo>) store.get(getName());

		if (records == null) {
			records = new ArrayList<>();
			records.addAll(accounts);

			store.put(getName(), records);
		} else {

			records.addAll(accounts);
		}
	}

	@Override
	public List<AccountInfo> getData() {
		return accounts;
	}

	@Override
	public List<AccountInfo> getDataFrom(Map<String, Object> store) {
		// TODO Auto-generated method stub
		return null;
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

	private boolean isTitleColumn(Row row) {
		return "使用者身份".equals(row.getCell(0).getStringCellValue());
	}

}