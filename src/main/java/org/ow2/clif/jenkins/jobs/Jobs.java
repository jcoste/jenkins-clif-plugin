package org.ow2.clif.jenkins.jobs;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import jenkins.model.Jenkins;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.FreeStyleProject;

public class Jobs {

	public static String toJob(String plan) {
  	return removeExtension(plan.replace('/', '-'));
  }

	public static String toPlan(String job) {
    return job.replace("-", "/") + ".ctp";
  }

	public static FreeStyleProject
	newJob(ItemGroup<? extends Item> jenkins, String name) {
    FreeStyleProject project = new FreeStyleProject(
  			jenkins,
  			name
  	);
    return project;
  }

	public static FreeStyleProject newJob(String name) {
		return newJob(Jenkins.getInstance(), name);
	}

}
