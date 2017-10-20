package models;

public class Step {

	private String desc;

	private String gherkinType;

	private Command command;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getGherkinType() {
		return gherkinType;
	}

	public void setGherkinType(String gherkinType) {
		this.gherkinType = gherkinType;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "Step [desc=" + desc + ", gherkinType=" + gherkinType + ", command=" + command + "]";
	}

}
