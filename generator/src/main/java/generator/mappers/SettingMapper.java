package generator.mappers;

import java.util.Map;

import generator.handlers.HandlerExecution;
import generator.handlers.SettingHandler;

public class SettingMapper extends AbstractExcelSheetMapper<Map<String, Map<String, Object>>> {

	@Override
	protected HandlerExecution<Map<String, Map<String, Object>>> defaultHandle() {
		return new SettingHandler(sheet);
	}

	@Override
	protected String getType() {
		return "settings";
	}
}
