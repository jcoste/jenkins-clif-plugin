/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
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

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import hudson.*;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;

/**
 * Represents the Clif installation on the system.
 *
 * @author Julien Coste
 */
public final class ClifInstallation
		extends ToolInstallation
		implements EnvironmentSpecific<ClifInstallation>, NodeSpecific<ClifInstallation> {

	private final String clifHudsonTarget;

	private final ClifProActiveConfig clifProActiveConfig;

	@DataBoundConstructor
	public ClifInstallation(String name, String home, List<? extends ToolProperty<?>> properties,
	                        String clifHudsonTarget, ClifProActiveConfig clifProActiveConfig) {
		super(name, launderHome(home), properties);
		this.clifHudsonTarget = clifHudsonTarget;
		this.clifProActiveConfig = clifProActiveConfig;

	}

	public String getClifHudsonTarget() {
		return clifHudsonTarget;
	}


	public ClifProActiveConfig getClifProActiveConfig() {
		return clifProActiveConfig;
	}

	public boolean isProactiveInstallation() {
		return clifProActiveConfig != null;
	}

	private static String launderHome(String home) {
		if (home.endsWith("/") || home.endsWith("\\")) {
			// see https://issues.apache.org/bugzilla/show_bug.cgi?id=26947
			// Ant doesn't like the trailing slash, especially on Windows
			return home.substring(0, home.length() - 1);
		}
		else {
			return home;
		}
	}

	/**
	 * Gets the executable path of this Clif on the given target system.
	 */
	public String getExecutable(Launcher launcher)
			throws IOException, InterruptedException {
		return launcher.getChannel().call(new Callable<String, IOException>() {
			private static final long serialVersionUID = 1L;

			public String call()
					throws IOException {
				File exe = getExeFile();
				if (exe.exists()) {
					return exe.getPath();
				}
				return null;
			}
		});
	}

	private File getExeFile() {
		String execName = Functions.isWindows() ? "ant.bat" : "ant";
		String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);

		return new File(home, "bin/" + execName);
	}

	/**
	 * Returns true if the executable exists.
	 */
	public boolean getExists()
			throws IOException, InterruptedException {
		return getExecutable(new Launcher.LocalLauncher(TaskListener.NULL)) != null;
	}

	private static final long serialVersionUID = 1L;

	public ClifInstallation forEnvironment(EnvVars environment) {
		return new ClifInstallation(getName(), environment.expand(getHome()), getProperties().toList(),
		                            getClifHudsonTarget(), clifProActiveConfig);
	}

	public ClifInstallation forNode(Node node, TaskListener log)
			throws IOException, InterruptedException {
		return new ClifInstallation(getName(), translateFor(node, log), getProperties().toList(),
		                            getClifHudsonTarget(), clifProActiveConfig);
	}

	@Extension
	public static class DescriptorImpl
			extends ToolDescriptor<ClifInstallation> {

		@Override
		public String getDisplayName() {
			return "Clif";
		}

		// for compatibility reasons, the persistence is done by ClifBuilder.DescriptorImpl
		@Override
		public ClifInstallation[] getInstallations() {
			return Hudson.getInstance().getDescriptorByType(ClifBuilder.DescriptorImpl.class).getInstallations();
		}

		@Override
		public void setInstallations(ClifInstallation... installations) {
			Hudson.getInstance().getDescriptorByType(ClifBuilder.DescriptorImpl.class).setInstallations(
					installations);
		}

		/**
		 * Checks if the CLIF_HOME is valid.
		 */
		public FormValidation doCheckHome(@QueryParameter File value) {
			// this can be used to check the existence of a file on the server, so needs to be protected
			if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) {
				return FormValidation.ok();
			}

			if (value.getPath().equals("")) {
				return FormValidation.error(Messages.Clif_HomeRequired());
			}

			if (!value.isDirectory()) {
				return FormValidation.error(Messages.Clif_NotADirectory(value));
			}

			File clifJar = new File(value, "lib" + File.separator + "clif-core.jar");
			if (!clifJar.exists()) {
				return FormValidation.error(Messages.Clif_NotClifDirectory(value));
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckName(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_NameRequired());
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckClifHudsonTarget(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_TargetRequired());
			}
			return FormValidation.ok();
		}

		/**
		 * Checks if the Scheduler url is valid.
		 *
		 * @param value
		 * @return
		 */
		public FormValidation doCheckSchedulerURL(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_SchedulerURLRequired());
			}
			return FormValidation.ok();
		}

		/**
		 * Checks if the scheduler credentials file is valid
		 *
		 * @param value
		 * @return
		 */
		public FormValidation doCheckSchedulerCredentialsFile(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_SchedulerCredentialsFileRequired());
			}
			return FormValidation.ok();
		}

		/**
		 *
		 */
		public FormValidation doCheckInstallation(
				@QueryParameter File home,
				@QueryParameter String schedulerURL,
				@QueryParameter File schedulerCredentialsFile) {
			try {
				if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER)) {
					return FormValidation.ok();
				}

				if (home.getPath().equals("")) {
					return FormValidation.error(Messages.Clif_HomeRequired());
				}

				if (!home.isDirectory()) {
					return FormValidation.error(Messages.Clif_NotADirectory(home));
				}

				File clifJar = new File(home, "lib" + File.separator + "clif-core.jar");
				if (!clifJar.exists()) {
					return FormValidation.error(Messages.Clif_NotClifDirectory(home));
				}
				File proactiveJar =
						new File(home, "lib" + File.separator + "ProActive" + File.separator + "ProActive.jar");
				if (!proactiveJar.exists()) {
					return FormValidation.error(Messages.ClifInstallation_BadProactiveInstallation());
				}

				if (schedulerCredentialsFile == null || schedulerCredentialsFile.getPath().equals("")) {
					return FormValidation.error(Messages.ClifInstallation_CredentialsFileMissing());

				}
				if (!schedulerCredentialsFile.exists()) {
					return FormValidation.error(Messages.ClifInstallation_CredentialsFileNotFound());
				}
				if (StringUtils.isBlank(schedulerURL)) {
					return FormValidation.error(Messages.ClifInstallation_SchedulerURLMissing());
				}

				return FormValidation.ok(Messages.ClifInstallation_ProactiveInstallationValid());
			}
			catch (Exception e) {
				//return FormValidation.errorWithMarkup("<p>"+Messages.Mailer_FailedToSendEmail()+"</p><pre>"+Util.escape(Functions.printThrowable(e))+"</pre>");
				return FormValidation.error(Messages.ClifInstallation_BadProactiveInstallation());
			}
		}
	}

}
