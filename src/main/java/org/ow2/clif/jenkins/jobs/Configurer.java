package org.ow2.clif.jenkins.jobs;

import hudson.model.FreeStyleProject;

import java.io.IOException;

import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPublisher;

public class Configurer {
	Installations installations = new Installations();

	public FreeStyleProject
	configure(FreeStyleProject project, String testPlan) throws IOException {
		String dir = Workspaces.DEFAULT_LOCATION, plan = null;
		if (testPlan != null) {
			String[] strings = testPlan.split("/");
			dir += "/" + strings[0];
			plan = strings[1];
		}
		project.getBuildersList().add(newClifBuilder(plan));
		project.getPublishersList().add(newClifPublisher(dir));
		project.setCustomWorkspace(dir);
		return project;
	}

	ClifPublisher newClifPublisher(String dir) {
	  return new ClifPublisher(dir + "/report");
  }

	ClifBuilder newClifBuilder(String plan) {
	  String clifName = installations.getFirstClifName();
		String antName = installations.getFirstAntName();
		ClifBuilder builder = new ClifBuilder(
				clifName, antName, null, null, plan, "report"
		);
	  return builder;
  }
}