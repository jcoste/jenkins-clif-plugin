package org.ow2.clif.jenkins.jobs;

import java.io.File;

import org.ow2.clif.jenkins.ClifPlugin;

import jenkins.model.Jenkins;

public class Workspaces {

	public static File dir() {
		return Jenkins.getInstance().getPlugin(ClifPlugin.class).dir();
	}

}
