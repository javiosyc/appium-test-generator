package models;

import java.util.List;

public class Feature {
	private String name;
	private String desc;

	private String packageName;

	private List<Scenario> scenarios;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String toString() {
		return "Feature [name=" + name + ", desc=" + desc + ", packageName=" + packageName + ", scenarios=" + scenarios
				+ "]";
	}

}
