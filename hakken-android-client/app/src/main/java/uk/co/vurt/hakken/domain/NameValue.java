package uk.co.vurt.hakken.domain;

public class NameValue {

	private String name;
	private String value;
	private Boolean singleSelect;

	public NameValue() {

	}

	public NameValue(String name, String value){
		this.name = name;
		this.value = value;
	}


	public NameValue(String name, String value, Boolean singleSelect){
		this.name = name;
		this.value = value;
		this.singleSelect = singleSelect;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString(){
		return this.name;
	}

	public Boolean getSingleSelect() {
		return singleSelect;
	}

	public void setSingleSelect(Boolean singleSelect) {
		this.singleSelect = singleSelect;
	}

}
