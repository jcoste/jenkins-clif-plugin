package org.ow2.clif.jenkins.jobs;

import java.io.File;

import hudson.model.FreeStyleProject;

public class FakeConfigurer extends Configurer {
	@Override
	public FreeStyleProject
	configure(FreeStyleProject project, File dir, String testPlan) {
		return project;
	}
}
