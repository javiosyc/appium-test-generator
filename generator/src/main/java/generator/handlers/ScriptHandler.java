package generator.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.google.common.collect.Iterators;

import models.Command;
import models.Feature;
import models.Scenario;
import models.Step;

public class ScriptHandler implements HandlerExecution<Feature> {

	private final XSSFSheet sheet;

	private Feature feature;

	private static final List<String> gherkins = Arrays.asList("Given", "And", "When", "Then");

	private static final String SCENARIO_NAME_TAG = "MethodName";
	private static final String SCENARIO_COMMENT_TAG = "MethodComment";

	public ScriptHandler(XSSFSheet sheet) {
		this.sheet = sheet;
		feature = new Feature();
		feature.setScenarios(new ArrayList<>());
	}

	@Override
	public void generate() {

		int rowNumber = populateClassInfo(sheet);

		Iterator<Row> rowIterator = sheet.iterator();
		Iterators.advance(rowIterator, rowNumber);

		Scenario currentScenario = null;

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();

			String firstCell = getStringCellValue(row, 0);

			if (StringUtils.isBlank(firstCell)) {
				continue;
			}

			if (gherkins.contains(firstCell)) {

				if (currentScenario != null) {
					String stepType = row.getCell(0).getStringCellValue();
					String desc = row.getCell(1).getStringCellValue();
					String commandType = row.getCell(2).getStringCellValue();

					Step step = new Step();
					step.setDesc(desc);

					if ("And".equals(stepType) && currentScenario.getSteps().size() > 0) {
						stepType = currentScenario.getSteps().get(currentScenario.getSteps().size() - 1)
								.getGherkinType();
					}

					step.setGherkinType(stepType);

					Command command = new Command();
					command.setType(commandType);

					step.setCommand(command);

					for (int cn = 3; cn < row.getLastCellNum(); cn++) {
						Cell cell = row.getCell(cn);
						CellType cellType = cell.getCellTypeEnum();
						switch (cellType) {
						case STRING:
							command.addParam(row.getCell(cn).getStringCellValue());
							break;
						case NUMERIC:
							command.addParam(row.getCell(cn).getNumericCellValue());
							break;
						default:
							break;
						}
					}

					currentScenario.getSteps().add(step);
				}
			} else if (SCENARIO_NAME_TAG.equals(firstCell)) {

				String name = row.getCell(1).getStringCellValue();
				currentScenario = new Scenario();
				currentScenario.setName(name);
				currentScenario.setSteps(new ArrayList<>());

				feature.getScenarios().add(currentScenario);

			} else if (SCENARIO_COMMENT_TAG.equals(firstCell)) {

				if (currentScenario != null) {
					String desc = row.getCell(1).getStringCellValue();
					currentScenario.setDesc(desc);
				}
			}

		}

	}

	private int populateClassInfo(XSSFSheet sheet) {
		String className = sheet.getRow(1).getCell(1).getStringCellValue();
		String desc = sheet.getRow(2).getCell(1).getStringCellValue();
		String packageName = sheet.getRow(3).getCell(1).getStringCellValue();

		feature.setName(className);
		feature.setDesc(desc);
		feature.setPackageName(packageName);

		return 4;
	}

	@Override
	public String getName() {
		return "script";
	}

	private String getStringCellValue(Row row, int i) {
		Cell cell = row.getCell(i);
		if (cell == null) {
			return "";
		}

		return cell.getStringCellValue();
	}

	@Override
	public List<Feature> getDataFrom(Map<String, Object> data) {

		List<Feature> records = (List<Feature>) data.get(getName());

		return CollectionUtils.isNotEmpty(records) ? new ArrayList<>() : records;
	}

	@Override
	public void addRecordTo(Map<String, Object> store) {

		List<Feature> records = (List<Feature>) store.get(getName());

		if (records == null) {
			records = new ArrayList<>();
			records.add(getData());
			store.put(getName(), records);
		} else {
			records.add(getData());
		}
	}

	@Override
	public Feature getData() {
		return feature;
	}
}
