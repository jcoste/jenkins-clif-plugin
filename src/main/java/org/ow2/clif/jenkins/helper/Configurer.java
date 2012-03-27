package org.ow2.clif.jenkins.helper;

import hudson.model.FreeStyleProject;
import hudson.tasks.Publisher;

import java.io.IOException;

import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPublisher;

public class Configurer {
	Installations installations = new Installations();

	public FreeStyleProject
	configure(FreeStyleProject project, String testPlan) throws IOException {
		project.getBuildersList().add(newClifBuilder(testPlan));
		project.getPublishersList().add(newClifPublisher());
		project.setCustomWorkspace(System.getProperty("user.home") + "/reports");
		return project;
	}

	Publisher newClifPublisher() {
	  return new ClifPublisher(System.getProperty("user.home") + "/reports", false, null, null, 0, 0, 0, 0, 0);
  }

	ClifBuilder newClifBuilder(String testPlan) {
	  String clifName = installations.getFirstClifName();
		String antName = installations.getFirstAntName();
		ClifBuilder builder = new ClifBuilder(
				clifName, antName, null, null, testPlan, null
		);
	  return builder;
  }
}