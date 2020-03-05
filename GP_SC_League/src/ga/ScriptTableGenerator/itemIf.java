package ga.ScriptTableGenerator;

import java.util.ArrayList;
import java.util.List;

public class itemIf {

	private  int MaxOpens;
	private boolean lastOpen;
	private String type;
	

	public itemIf(int MaxOpens, boolean lastOpen, String type)
	{
		this.MaxOpens=MaxOpens;
		this.lastOpen=lastOpen;
		this.type=type;
		
	}

	/**
	 * @return the maxOpens
	 */
	public int getMaxOpens() {
		return MaxOpens;
	}

	/**
	 * @return the lastOpen
	 */
	public boolean isLastOpen() {
		return lastOpen;
	}
	
	/**
	 * @param maxOpens the maxOpens to set
	 */
	public void setMaxOpens(int maxOpens) {
		MaxOpens = maxOpens;
	}

	/**
	 * @param lastOpen the lastOpen to set
	 */
	public void setLastOpen(boolean lastOpen) {
		this.lastOpen = lastOpen;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
