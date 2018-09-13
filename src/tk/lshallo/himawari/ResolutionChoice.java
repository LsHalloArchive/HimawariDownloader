package tk.lshallo.himawari;

public class ResolutionChoice {
	private String name;
	private int value;
	
	public ResolutionChoice(String name, int val) {
		this.name = name;
		this.value = val;
	}
	
	public String getName() {
		return name;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
