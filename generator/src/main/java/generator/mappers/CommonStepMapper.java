package generator.mappers;

import generator.handlers.CommonStepHandler;
import generator.handlers.HandlerExecution;
import models.CommonUtilClass;

/**
 * 判斷登入情境共用步驟commonStepSheet
 *
 * @author Cyndi
 *
 */
public class CommonStepMapper extends AbstractExcelSheetMapper<CommonUtilClass> {

	public static final String TYPE = "commonStep";

	@Override
	protected HandlerExecution<CommonUtilClass> defaultHandle() {
		return new CommonStepHandler(sheet, getType());
	}

	@Override
	protected String getType() {
		return TYPE;
	}
}
