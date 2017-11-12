package generator.mappers;

import generator.handlers.HandlerExecution;
import generator.handlers.ScriptHandler;
import models.Feature;

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
