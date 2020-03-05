package SetCoverSampling;

public class StateAction {
	
	public StateAction()
	{

	}
	
	public StateAction(String state, String action, String nameState, String counterByFunction)
	{
		this.state=state;
		this.action=action;
		this.nameState=nameState;
		this.counterByFunction=counterByFunction;
	}
	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	/**
	 * @return the nameState
	 */
	public String getNameState() {
		return nameState;
	}

	/**
	 * @param nameState the nameState to set
	 */
	public void setNameState(String nameState) {
		this.nameState = nameState;
	}

	public String getCounterByFunction() {
		return counterByFunction;
	}

	public void setCounterByFunction(String counterByFunction) {
		this.counterByFunction = counterByFunction;
	}

	private String state;
	private String action;
	private String nameState;
	private String counterByFunction;

	


}
