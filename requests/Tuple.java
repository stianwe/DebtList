package requests;

public class Tuple {

	private String variableName;
	private Object variable;
	
	public Tuple(String variableName, Object variable) {
		this.variableName = variableName;
		this.variable = variable;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public Object getVariable() {
		return variable;
	}
}
