package org.ow2.clif.jenkins.helper;

import hudson.model.FreeStyleProject;

public class FakeConfigurer extends Configurer {
	@Override
  public FreeStyleProject configure(FreeStyleProject project, String testPlan) {
	  return project;
  }
}
