package generator.mappers;

import java.util.Map;

import generator.handlers.HandlerExecution;
import generator.handlers.SettingHandler;

/**
 * 判斷裝置版本Sheet
 *
 * @author Cyndi
 *
 */
public class SettingMapper extends AbstractExcelSheetMapper<Map<String, Map<String, Object>>> {

	public static final String TYPE = "settings";

	@Override
	protected HandlerExecution<Map<String, Map<String, Object>>> defaultHandle() {
		return new SettingHandler(sheet, getType());
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
