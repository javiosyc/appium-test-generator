package models;

import java.util.List;

/**
 * 登入情境共用模組
 * 
 * @author Cyndi
 *
 */
public class CommonUtilClass {
	/**
	 * ClassComment
	 */
	private String desc;

	private List<CommonMethod> methods;
	/**
	 * ClassName
	 */
	private String name;

	/**
	 * PackageName
	 */
	private String packageName;

	public String getDesc() {
		return desc;
	}

	public List<CommonMethod> getMethods() {
		return methods;
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setMethods(List<CommonMethod> methods) {
		this.methods = methods;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
