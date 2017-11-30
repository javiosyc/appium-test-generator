package models;

/**
 * 步驟
 * 
 * @author Cyndi
 *
 */
public class Step {

	private Command command;

	private String desc;

	private String gherkinType;

	public Command getCommand() {
		return command;
	}

	public String getDesc() {
		return desc;
	}

	public String getGherkinType() {
		return gherkinType;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setGherkinType(String gherkinType) {
		this.gherkinType = gherkinType;
	}

	@Override
	public String toString() {
		return "Step [desc=" + desc + ", gherkinType=" + gherkinType + ", command=" + command + "]";
	}

}
