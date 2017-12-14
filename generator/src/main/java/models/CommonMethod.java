package models;

import java.util.List;

/**
 * 各種登入共用方法CommonMethod
 * 
 * @author Cyndi
 *
 */
public class CommonMethod {

	/**
	 * ClassName
	 */
	private String className;
	/**
	 * ClassComment
	 */
	private String desc;

	/**
	 * MethodName
	 */
	private String name;

	/**
	 * noReset Type
	 */
	private boolean noReset = false;

	/**
	 * package name
	 */
	private String packageName;

	/**
	 * Step
	 */
	private List<Step> steps;

	public String getClassName() {
		return className;
	}

	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public String getPackageName() {
		return packageName;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public boolean isNoReset() {
		return noReset;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNoReset(boolean noReset) {
		this.noReset = noReset;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

}
