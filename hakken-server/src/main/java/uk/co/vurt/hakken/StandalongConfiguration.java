package uk.co.vurt.hakken;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.co.vurt.hakken.security.auth.AllowAlwaysAuthenticator;
import uk.co.vurt.hakken.security.auth.Authenticator;
import uk.co.vurt.hakken.server.task.TaskFileSourceService;
import uk.co.vurt.hakken.server.task.TaskSourceService;

@Configuration
public class StandalongConfiguration {
    @Inject
    private Environment environment;

	@Bean
	TaskSourceService taskSource() {
		TaskFileSourceService tss = new TaskFileSourceService();
		tss.setTaskDirectoryPaths(environment.getProperty("taskdefinition.src.dirs", String.class, (String)null));
		return tss;
	}

	@Bean
	Authenticator authenticator() {
		return new AllowAlwaysAuthenticator();
	}
}
