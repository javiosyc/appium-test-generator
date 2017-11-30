package models;

import java.util.ArrayList;
import java.util.List;

/**
 * 各種appium指令
 * 
 * @author Cyndi
 *
 */
public class Command {
	/**
	 * Command 參數(ElementName、Action、Value)
	 */
	private List<Object> params = new ArrayList<>();

	/**
	 * Command Type
	 */
	private String type;

	public void addParam(Object param) {
		params.add(param);
	}

	public List<Object> getParams() {
		return params;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Command [type=" + type + ", params=" + params + "]";
	}

}
