package org.ow2.clif.jenkins.helper;

import hudson.tasks.Ant.AntInstallation;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;

import org.ow2.clif.jenkins.ClifInstallation;

/**
 * boilerplate
 *
 */
public class Installations {
	Jenkins jenkins = Jenkins.getInstance();

	String getFirstClifName() {
    return firstName(getInstallations(ClifInstallation.DescriptorImpl.class));
  }

	String getFirstAntName() {
		return firstName(getInstallations(AntInstallation.DescriptorImpl.class));
	}

	@SuppressWarnings("rawtypes")
  private ToolInstallation[]
			getInstallations(Class<? extends ToolDescriptor> descriptor) {
		return jenkins.getDescriptorByType(descriptor).getInstallations();
	}

	private String firstName(ToolInstallation[] installations) {
	  if(installations.length == 0) {
    	return null;
    }
		return installations[0].getName();
  }
}
