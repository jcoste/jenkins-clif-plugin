package org.ow2.clif.jenkins.jobs;

import hudson.model.FreeStyleProject;

import java.io.IOException;

import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPublisher;

public class Configurer {
	Installations installations = new Installations();

	public FreeStyleProject
	configure(FreeStyleProject job, String plan) throws IOException {
		String dir = Workspaces.DEFAULT_LOCATION, p = null;
		if (plan != null) {
			String[] strings = plan.split("/");
			dir += "/" + strings[0];
			p = strings[1];
		}
		job.getBuildersList().add(newClifBuilder(p));
		job.getPublishersList().add(newClifPublisher(dir));
		job.setCustomWorkspace(dir);
		return job;
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