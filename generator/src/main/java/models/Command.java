package models;

import java.util.ArrayList;
import java.util.List;

public class Command {

	private String type;

	private List<Object> params = new ArrayList<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Object> getParams() {
		return params;
	}

	public void addParam(Object param) {
		params.add(param);
	}

	@Override
	public String toString() {
		return "Command [type=" + type + ", params=" + params + "]";
	}

}
