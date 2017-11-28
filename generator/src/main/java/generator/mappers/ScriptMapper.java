package generator.mappers;

import generator.handlers.HandlerExecution;
import generator.handlers.ScriptHandler;
import models.Feature;

/**
 * 判斷測試腳本(Script)Sheet
 *
 * @author Cyndi
 *
 */
public class ScriptMapper extends AbstractExcelSheetMapper<Feature> {

	@Override
	protected HandlerExecution<Feature> defaultHandle() {
		return new ScriptHandler(sheet);
	}

	@Override
	protected String getType() {
		return "script";
	}
}
