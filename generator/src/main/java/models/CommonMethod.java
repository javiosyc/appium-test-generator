package models;

import java.util.List;

public class CommonMethod {
	private String name;
	private String desc;

	private String packageName;
	private String className;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	private boolean noReset = true;
	private List<Step> steps;

	public String getName() {
		return name;
	}

	public boolean isNoReset() {
		return noReset;
	}

	public void setNoReset(boolean noReset) {
		this.noReset = noReset;
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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

}
