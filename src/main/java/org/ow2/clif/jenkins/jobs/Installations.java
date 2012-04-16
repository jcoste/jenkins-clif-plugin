package org.ow2.clif.jenkins.jobs;

import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;

/**
 * boilerplate
 */
public class Installations {

	public Installations() {
	}

	public <I extends ToolInstallation> I[]
			all(Class<? extends ToolDescriptor<I>> descriptor) {
		return Jenkins.getInstance().getDescriptorByType(descriptor).getInstallations();
	}

	public <I extends ToolInstallation> I
		first(Class<? extends ToolDescriptor<I>> descriptor, String name) {
		for (I installation : all(descriptor)) {
			if (name == null || name.equals(installation.getName())) {
				return installation;
			}
		}
		return null;
	}

	public <I extends ToolInstallation> I
	first(Class<? extends ToolDescriptor<I>> descriptor) {
		return first(descriptor, null);
	}

	public <I extends ToolInstallation> String
	firstName(Class<? extends ToolDescriptor<I>> descriptor) {
		I install = first(descriptor, null);
		return install == null ? null : install.getName();
	}

}
