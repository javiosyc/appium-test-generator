package models;

import java.util.List;

public class Feature {
	private String name;
	private String desc;

	private String group;

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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	@Override
	public String toString() {
		return "Feature [name=" + name + ", desc=" + desc + ", group=" + group + ", scenarios=" + scenarios + "]";
	}

}
