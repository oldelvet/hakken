package uk.co.vurt.hakken.domain.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Submission implements Serializable {

	private static final long serialVersionUID = 2876749616083811529L;
	
	private Long id;
	private String username;
	private Long jobId;
	private String remoteId;
	private String taskDefinitionName;
	private List<DataItem> dataItems;
	private Date timestamp;
	private String status;
	
	public Submission(){
		dataItems = new ArrayList<DataItem>();
	}
	
	public Submission(String username, long jobId, List<DataItem> dataItems) {
		this();
		this.username = username;
		this.jobId = jobId;
		this.dataItems = dataItems;
	}

	public Submission(Long id, String username, long jobId, List<DataItem> dataItems){
		this(username, jobId, dataItems);
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public List<DataItem> getDataItems() {
		return dataItems;
	}

	public void setDataItems(List<DataItem> dataItems) {
		this.dataItems = dataItems;
	}
	
	public void addDataItem(DataItem dataItem){
		this.dataItems.add(dataItem);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTaskDefinitionName() {
		return taskDefinitionName;
	}

	public void setTaskDefinitionName(String taskDefinitionName) {
		this.taskDefinitionName = taskDefinitionName;
	}

	public String getRemoteId() {
        return remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Submission [username=" + username + ", jobId=" + jobId
				+ ", remoteId=" + remoteId + ", dataItems=" + dataItems + "]";
	}
	
	
}
