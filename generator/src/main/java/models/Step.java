package models;

/**
 * 步驟
 * 
 * @author Cyndi
 *
 */
public class Step {

	/**
	 * Command(example:ByName,CheckAlert,ByXpath...etc)
	 */
	private Command command;

	/**
	 * Step步驟描述
	 */
	private String desc;

	/**
	 * GWT(example:Given,When,Then)
	 */
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
