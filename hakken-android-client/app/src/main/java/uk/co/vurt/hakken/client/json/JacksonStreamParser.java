package uk.co.vurt.hakken.client.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.co.vurt.hakken.domain.job.DataItem;
import uk.co.vurt.hakken.domain.job.JobDefinition;
import uk.co.vurt.hakken.domain.task.TaskDefinition;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonStreamParser implements JsonStreamParser {

	private final static String TAG = "JacksonStreamParser";
	
	private JsonFactory jsonFactory;
	
	public JacksonStreamParser(){
		jsonFactory = new JsonFactory();
	}
	
	@Override
	public List<JobDefinition> parseJobDefinitionStream(InputStream in) throws IOException {
		JsonParser jp = jsonFactory.createJsonParser(in);
		try{
			return readJobsArray(jp, null);
		}finally{
			jp.close();
		}
	}

	@Override
	public void parseJobDefinitionStream(InputStream in,
			JobDefinitionHandler callback) throws IOException {
		JsonParser jp = jsonFactory.createJsonParser(in);
		try{
			readJobsArray(jp, callback);
		}finally{
			jp.close();
		}
	}
	
	@Override
	public List<TaskDefinition> parseTaskDefinitionStream(InputStream in)
			throws IOException {
		JsonParser jp = jsonFactory.createJsonParser(in);
		try{
			return readTaskDefinitionArray(jp, null);
		}finally{
			jp.close();
		}
	}

	@Override
	public void parseTaskDefinitionStream(InputStream in,
			TaskDefinitionHandler callback) throws IOException {
		JsonParser jp = jsonFactory.createJsonParser(in);
		try{
			readTaskDefinitionArray(jp, callback);
		}finally{
			jp.close();
		}
	}
	
	private List<JobDefinition> readJobsArray(JsonParser jp, JobDefinitionHandler callback) throws IOException{
		List<JobDefinition> jobs = new ArrayList<JobDefinition>();
		
		JsonToken current;
		
		try{
			current = jp.nextToken();
			if(current != JsonToken.START_ARRAY){
				throw new IOException("Error: root should be array.");
			}
			
			current = jp.nextToken();
			while(current != null && current != JsonToken.END_ARRAY){
				if(callback != null){
					callback.handle(readJob(jp));
				}else {
					jobs.add(readJob(jp));
				}
				current = jp.nextToken();
			}
		}catch(JsonParseException jpe){
			throw new IOException(jpe.getMessage(), jpe);
		}
		
		return jobs;
	}
	
	private List<TaskDefinition> readTaskDefinitionArray(JsonParser jp, TaskDefinitionHandler callback) throws IOException{
		List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();
		
		JsonToken current;
		
		try{
			current = jp.nextToken();
			if(current != JsonToken.START_ARRAY){
				throw new IOException("Error: root should be array.");
			}
			
			current = jp.nextToken();
			while(current != null && current != JsonToken.END_ARRAY){
				if(callback != null){
					callback.handle(readTaskDefinition(jp));
				}else {
					taskDefinitions.add(readTaskDefinition(jp));
				}
				current = jp.nextToken();
			}
		}catch(JsonParseException jpe){
			throw new IOException(jpe.getMessage(), jpe);
		}
		
		return taskDefinitions;
	}
	
	private JobDefinition readJob(JsonParser jp) throws IOException, JsonParseException{
		Long id = null;
		String remoteId = null;
		String name = null;
		Long taskDefinitionId = null;
		Date created = null;
		Date due = null;
		String status = null;
		String group = null;
		String notes = null;
		String description = null;
		Set<DataItem> dataItems = new HashSet<DataItem>();
		boolean modified = false;
		
		JsonToken current = jp.getCurrentToken();
		if(current != JsonToken.START_OBJECT){
			throw new IOException("Error: expected start of object, instead found " + current.asString());
		}
		while(jp.nextToken() != JsonToken.END_OBJECT){
			String itemName = jp.getCurrentName();
			//move to value token
			jp.nextToken();
			if(jp.getCurrentToken() != JsonToken.VALUE_NULL){
				
				Log.e("CK_ITEM", jp.getText());
				
				if(itemName.equals("id")){
					id = jp.getValueAsLong();
				} else if(itemName.equals("remoteId")) {
				    remoteId = jp.getText();
				} else if(itemName.equals("name")){
					name = jp.getText();
				} else if(itemName.equals("taskDefinitionId")){
					taskDefinitionId = jp.getValueAsLong();
				} else if(itemName.equals("created")){
					created = new Date(Long.parseLong(jp.getText()));
				} else if(itemName.equals("due")){
					due = new Date(Long.parseLong(jp.getText()));
				} else if(itemName.equals("status")){
					status = jp.getText();
				} else if(itemName.equals("group")){
					group = jp.getText();
				} else if(itemName.equals("notes")){
					Log.e("CK_NOTES", jp.getText());
					notes = jp.getText();
				} else if(itemName.equals("description")){
					Log.e("CK_DESC", jp.getText());
					description = jp.getText();					
				} else if(itemName.equals("dataItems")){
					dataItems.addAll(readDataItemsArray(jp));
				} else if(itemName.equals("modified")){
					modified = jp.getBooleanValue();
				} else {
					//something we weren't expecting so just skip it.
					jp.skipChildren();
				}
			}
		}

		return new JobDefinition(id, remoteId, name, taskDefinitionId, created, due, status, group, notes, description, dataItems, modified);
	}


	private List<DataItem> readDataItemsArray(JsonParser jp) throws IOException, JsonParseException {
		List<DataItem> dataItems = new ArrayList<DataItem>();
		JsonToken current = jp.getCurrentToken();
		if(current != JsonToken.START_ARRAY){
			throw new IOException("Error: expected start of array.");
		}
		while(jp.nextToken() != JsonToken.END_ARRAY){
			dataItems.add(readDataItem(jp));
		}
		return dataItems;
	}
	
	private DataItem readDataItem(JsonParser jp) throws IOException, JsonParseException {
		Long id = null;
		String name = null;
		String type = null;
		String value = null;
		String pageName = null;
		
		if(jp.getCurrentToken() != JsonToken.START_OBJECT){
			throw new IOException("Error: expected start of object");
		}
		while(jp.nextToken() != JsonToken.END_OBJECT){
			String itemName = jp.getCurrentName();
			jp.nextToken();
			if(jp.getCurrentToken() != JsonToken.VALUE_NULL){
				if(itemName.equals("id")){
					id = jp.getValueAsLong();
				} else if(itemName.equals("name")){
					name = jp.getText();
				} else if(itemName.equals("type")){
					type = jp.getText();
				} else if(itemName.equals("value")){
					
					value = jp.getText();
				} else if(itemName.equals("pageName")){
					pageName = jp.getText();
				} else {
					jp.skipChildren(); //something we weren't expecting, so ignore it
				}
			}
		}
		
		return new DataItem(id, pageName, name, type, value);
	}

	private TaskDefinition readTaskDefinition(JsonParser jp) throws IOException, JsonParseException{
		//Use object mapper rather than stream parsing for the time being.
		ObjectMapper mapper = new ObjectMapper();
		TaskDefinition taskDefinition = mapper.readValue(jp, TaskDefinition.class);
		return taskDefinition;
	}

}
