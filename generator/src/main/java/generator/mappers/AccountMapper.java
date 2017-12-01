package generator.mappers;

import java.util.List;

import generator.handlers.AccountHandler;
import generator.handlers.HandlerExecution;
import models.AccountInfo;

/**
 * 判斷測試資料Sheet
 *
 * @author Cyndi
 *
 */
public class AccountMapper extends AbstractExcelSheetMapper<List<AccountInfo>> {

	public static final String TYPE = "data";

	@Override
	protected HandlerExecution<List<AccountInfo>> defaultHandle() {
		return new AccountHandler(sheet, getType());
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
