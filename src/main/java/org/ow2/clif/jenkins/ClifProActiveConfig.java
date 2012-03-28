/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2004, 2008 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins;

import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.Serializable;

/**
 * @author Julien Coste
 */
public class ClifProActiveConfig
		implements Serializable, Describable<ClifProActiveConfig> {

	private static final long serialVersionUID = 1L;

	/**
	 * The url of the ProActive Scheduler;
	 */
	private String schedulerURL;

	/**
	 * The absolute path of the scheduler credentials file on the Jenkins server file system
	 */
	private String schedulerCredentialsFile;

	/**
	 * Props are the appropriate ProActive properties to use for the forked JVM.
	 */
	private String forkProps;

	public ClifProActiveConfig() {
	}


	@DataBoundConstructor
	public ClifProActiveConfig(String schedulerURL, String schedulerCredentialsFile, String forkProps) {
		super();
		this.schedulerURL = Util.fixEmptyAndTrim(schedulerURL);
		this.schedulerCredentialsFile = schedulerCredentialsFile;
		this.forkProps = forkProps;
	}

	public Descriptor<ClifProActiveConfig> getDescriptor() {
		return Hudson.getInstance().getDescriptorByType(ProActiveConfigDescriptor.class);
	}

	@Extension
	public static final class ProActiveConfigDescriptor
			extends Descriptor<ClifProActiveConfig> {

		@Override
		public String getDisplayName() {
			return "";
		}


	}

	public String getSchedulerURL() {
		return schedulerURL;
	}

	public String getSchedulerCredentialsFile() {
		return schedulerCredentialsFile;
	}

	public String getForkProps() {
		return forkProps;
	}

}
