package uk.co.vurt.hakken.domain.task.pageitem;

public class LabelledValue {

	String label;
	String value;
	Boolean singleSelect;
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Boolean getSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(Boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

	
	@Override
	public String toString() {
		return "LabelledValue [label=" + label + ", value=" + value + "]";
	}
	
}
