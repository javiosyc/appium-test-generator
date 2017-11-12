package generator.mappers;

import generator.handlers.CommonStepHandler;
import generator.handlers.HandlerExecution;
import models.CommonUtilClass;

public class CommonStepMapper extends AbstractExcelSheetMapper<CommonUtilClass> {

	@Override
	protected HandlerExecution<CommonUtilClass> defaultHandle() {
		return new CommonStepHandler(sheet);
	}

	@Override
	protected String getType() {
		return "utils";
	}
}
