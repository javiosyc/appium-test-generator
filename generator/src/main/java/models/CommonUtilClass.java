package models;

import java.util.List;

public class CommonUtilClass {
	private String name;
	private String desc;

	private String packageName;

	private List<CommonMethod> methods;

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

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public List<CommonMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<CommonMethod> methods) {
		this.methods = methods;
	}

}
