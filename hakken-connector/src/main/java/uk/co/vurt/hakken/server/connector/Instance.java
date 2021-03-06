package uk.co.vurt.hakken.server.connector;

import java.util.Date;
import java.util.Map;


public class Instance {

	private String id;
	private String name;
	private Date created;
	private Date due;
	private String notes;
	private String description;
	private Map<String, String> dataItems;
	
	public Instance(){}
	
	public Instance(String id, String name, Date created, Date due, String notes,
			Map<String, String> dataItems) {
		super();
		this.id = id;
		this.name = name;
		this.created = created;
		this.due = due;
		this.notes = notes;
		this.dataItems = dataItems;
	}
	
	public Instance(String id, String name, Date created, Date due, String notes,
			String description, Map<String, String> dataItems) {
		super();
		this.id = id;
		this.name = name;
		this.created = created;
		this.due = due;
		this.notes = notes;
		this.description = description;
		this.dataItems = dataItems;
	}	

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public Date getCreated() {
		return created;
	}
	public Date getDue() {
		return due;
	}
	public Map<String, String> getDataItems() {
		return dataItems;
	}
	public void setDataItems(Map<String, String> dataItems) {
		this.dataItems = dataItems;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Instance [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", created=");
		builder.append(created);
		builder.append(", due=");
		builder.append(due);
		builder.append(", notes=");
		builder.append(notes);
		builder.append(", dataItems=");
		builder.append(dataItems);
		builder.append("]");
		return builder.toString();
	}
	
	
}
