package uk.co.vurt.hakken.server.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import uk.co.vurt.hakken.domain.job.Submission;
import uk.co.vurt.hakken.domain.job.SubmissionStatus;
import uk.co.vurt.hakken.security.HashUtils;
import uk.co.vurt.hakken.server.exception.HakkenException;
import uk.co.vurt.hakken.server.service.DataConnectorService;
import uk.co.vurt.hakken.server.service.JobService;
import uk.co.vurt.hakken.server.service.MappingService;
import uk.co.vurt.hakken.server.service.SubmissionService;

@Controller
@RequestMapping("/submissions")
public class SubmissionController extends RESTController{

	private static final Logger logger = LoggerFactory.getLogger(SubmissionController.class);
	
	@Autowired
	private SubmissionService service;
	@Autowired
	private JobService jobService;
	@Autowired 
	private MappingService mappingService;
	@Autowired
	DataConnectorService connectorService;
	
	@RequestMapping(value = "from/{username}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public @ResponseBody SubmissionStatus handleSubmission(@PathVariable String username, @RequestParam String hmac, @RequestBody Submission submission) throws HakkenException{
		SubmissionStatus status;
		Map<String, String>parameterMap = new HashMap<String, String>();
		parameterMap.put("username", username);

		logger.info("username: " + username);
		logger.info("hmac: " + hmac);
		
		boolean validRequest = HashUtils.validate(parameterMap, hmac);
		
		if(validRequest){
			logger.info("Received submission from " + username);
			logger.info(submission.toString());
	
			//Persist the submission in the Hakken audit tables
			service.store(submission);

			// Try to submit the 
			status = service.submit(submission);
			
			
			logger.debug("Submission status: " + status );
			
			// If sent to dataconnector successfully, then update submission entry
			service.setStatus(submission, status);
			
		}else {
			logger.warn("Woop Woop! Invalid request received!");
			throw new HakkenException("Invalid request received");
		}
		return status;
	}
	
}
