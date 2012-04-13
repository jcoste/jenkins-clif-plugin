package org.ow2.clif.jenkins.jobs;

import org.ow2.clif.jenkins.ClifPlugin;

import jenkins.model.Jenkins;

public class Workspaces {

	public static String location() {
		return Jenkins.getInstance().getPlugin(ClifPlugin.class).getClifRootDir();
	}

}
