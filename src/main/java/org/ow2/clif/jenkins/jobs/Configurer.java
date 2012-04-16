package org.ow2.clif.jenkins.jobs;

import hudson.model.FreeStyleProject;
import hudson.tasks.Ant.AntInstallation;

import java.io.File;
import java.io.IOException;

import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifInstallation;
import org.ow2.clif.jenkins.ClifJobProperty;
import org.ow2.clif.jenkins.ClifPublisher;

public class Configurer {
	Installations installations = new Installations();
	private String version;

	public FreeStyleProject
	configure(FreeStyleProject job, File dir, String plan) throws IOException {
		String directory = dir.getPath(), p = null;
		if (plan != null) {
			String[] strings = plan.split("/");
			directory += "/" + strings[0];
			p = strings[1];
		}
		job.getBuildersList().add(newClifBuilder(p));
		job.getPublishersList().add(newClifPublisher(directory));
		job.setCustomWorkspace(directory);
		job.addProperty(new ClifJobProperty(true));
		return job;
	}

	ClifPublisher newClifPublisher(String dir) {
		return new ClifPublisher(dir + "/report");
	}

	ClifBuilder newClifBuilder(String plan) {
		String clifName = version == null
				? installations.firstName(ClifInstallation.DescriptorImpl.class)
				: version;
		String antName =
				installations.firstName(AntInstallation.DescriptorImpl.class);
		ClifBuilder builder = new ClifBuilder(
				clifName, antName, null, null, plan, "report"
		);
		return builder;
	}

	public void use(String version) {
		// FIXME use(ClifInstallation) could be a better prototype
		// would require work in ClifBuilder though...
		this.version = version;
  }

}