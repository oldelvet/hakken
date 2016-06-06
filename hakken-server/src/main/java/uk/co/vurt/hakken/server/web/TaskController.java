package uk.co.vurt.hakken.server.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import uk.co.vurt.hakken.domain.task.TaskDefinition;
import uk.co.vurt.hakken.server.task.TaskRegistry;


@Controller
@RequestMapping("/tasks")
public class TaskController {

	private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
	
	@Autowired
	TaskRegistry taskRegistry;
	
	@RequestMapping(value="{id}", method=RequestMethod.GET)
	public @ResponseBody TaskDefinition getTaskById(@PathVariable long id){
		logger.debug("Getting task definition by id");
		return taskRegistry.getTask(id);
	}
	
	@RequestMapping(value="{name}", method=RequestMethod.GET)
	public @ResponseBody TaskDefinition getTaskByName(@PathVariable String name){
		logger.debug("Getting task definition by name");
		return taskRegistry.getTask(name);
	}
	
	//TODO: RP/Kash - DONE - method to return all task definitions
	@RequestMapping("list")
	public @ResponseBody List<TaskDefinition> getTaskDefinitions(){
		logger.debug("Getting task definitions");
		return taskRegistry.getAllTasks();
	}
}
