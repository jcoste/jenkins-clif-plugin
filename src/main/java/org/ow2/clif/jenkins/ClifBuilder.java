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

import hudson.*;
import hudson.model.*;
import hudson.tasks.Ant;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks._ant.AntConsoleAnnotator;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Clif launcher.
 *
 * @author Julien Coste
 */
public class ClifBuilder
		extends Builder {
	/**
	 * Identifies {@link ClifInstallation} to be used.
	 */
	private final String clifName;

	/**
	 * Identifies {@link hudson.tasks.Ant.AntInstallation} to be used.
	 */
	private final String antName;

	/**
	 * CLIF_OPTS if not null.
	 */
	private final String clifOpts;

	/**
	 * Optional properties to be passed to Clif. Follows {@link java.util.Properties} syntax.
	 */
	private final String properties;

	/**
	 * TestPlan file (ctp) to run
	 */
	private final String testPlanFile;

	/**
	 * Directory where Clif report is générated
	 */
	private final String reportDir;


	@DataBoundConstructor
	public ClifBuilder(String clifName, String antName, String clifOpts, String properties, String testPlanFile,
	                   String reportDir) {
		this.clifName = clifName;
		this.antName = antName;
		this.clifOpts = Util.fixEmptyAndTrim(clifOpts);
		this.properties = Util.fixEmptyAndTrim(properties);
		this.testPlanFile = testPlanFile;
		this.reportDir = reportDir;
	}

	public String getProperties() {
		return properties;
	}

	public String getTestPlanFile() {
		return testPlanFile;
	}

	public String getReportDir() {
		return reportDir;
	}

	/**
	 * Gets the Clif to invoke,
	 * or null to invoke the default one.
	 */
	public ClifInstallation getClif() {
		for (ClifInstallation i : getDescriptor().getInstallations()) {
			if (clifName != null && clifName.equals(i.getName())) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Gets the Ant to invoke,
	 * or null to invoke the default one.
	 */
	public Ant.AntInstallation getAnt() {
		Ant.AntInstallation[] antInst =
				Hudson.getInstance().getDescriptorByType(Ant.DescriptorImpl.class).getInstallations();
		if (antInst != null) {
			for (Ant.AntInstallation i : antInst) {
				if (antName != null && antName.equals(i.getName())) {
					return i;
				}
			}
		}
		return null;
	}

	/**
	 * Gets the CLIF_OPTS parameter, or null.
	 */
	public String getClifOpts() {
		return clifOpts;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		ClifInstallation clifInst = getClif();
		if (clifInst == null) {
			listener.fatalError(Messages.Clif_ClifInstallationNotFound());
			return false;
		}

		String overridedProperties = getOverridedProperties();

		ArgumentListBuilder args = new ArgumentListBuilder();
		EnvVars env = build.getEnvironment(listener);

		Ant.AntInstallation ai = getAnt();
		if (ai == null) {
			args.add(launcher.isUnix() ? "ant" : "ant.bat");
		}
		else {
			ai = ai.forNode(Computer.currentComputer().getNode(), listener);
			ai = ai.forEnvironment(env);
			String exe = ai.getExecutable(launcher);
			if (exe == null) {
				listener.fatalError(hudson.tasks.Messages.Ant_ExecutableNotFound(ai.getName()));
				return false;
			}
			args.add(exe);
		}

		VariableResolver<String> vr = build.getBuildVariableResolver();

		String buildFile = getClif().getHome() + File.separator + "build.xml";
		String targets = clifInst.getClifHudsonTarget();

		FilePath buildFilePath = buildFilePath(build.getModuleRoot(), buildFile, targets);

		if (!buildFilePath.exists()) {
			// because of the poor choice of getModuleRoot() with CVS/Subversion, people often get confused
			// with where the build file path is relative to. Now it's too late to change this behavior
			// due to compatibility issue, but at least we can make this less painful by looking for errors
			// and diagnosing it nicely. See HUDSON-1782

			// first check if this appears to be a valid relative path from workspace root
			FilePath buildFilePath2 = buildFilePath(build.getWorkspace(), buildFile, targets);
			if (buildFilePath2.exists()) {
				// This must be what the user meant. Let it continue.
				buildFilePath = buildFilePath2;
			}
			else {
				// neither file exists. So this now really does look like an error.
				listener.fatalError("Unable to find build script at " + buildFilePath);
				return false;
			}
		}

		if (buildFile != null) {
			args.add("-file", buildFilePath.getName());
		}

		Set<String> sensitiveVars = build.getSensitiveBuildVariables();

		args.addKeyValuePairs("-D", build.getBuildVariables(), sensitiveVars);

		args.addKeyValuePairsFromPropertyString("-D", overridedProperties, vr, sensitiveVars);

		args.addTokenized(targets.replaceAll("[\t\r\n]+", " "));

		if (ai != null) {
			env.put("ANT_HOME", ai.getHome());
		}
		if (clifOpts != null) {
			env.put("ANT_OPTS", env.expand(clifOpts));
		}

		if (!launcher.isUnix()) {
			args = args.toWindowsCommand();
			// For some reason, ant on windows rejects empty parameters but unix does not.
			// Add quotes for any empty parameter values:
			List<String> newArgs = new ArrayList<String>(args.toList());
			newArgs.set(newArgs.size() - 1,
			            newArgs.get(newArgs.size() - 1).replaceAll("(?<= )(-D[^\" ]+)= ", "$1=\"\" "));
			args = new ArgumentListBuilder(newArgs.toArray(new String[newArgs.size()]));
		}

		long startTime = System.currentTimeMillis();
		try {
			AntConsoleAnnotator aca = new AntConsoleAnnotator(listener.getLogger(), build.getCharset());
			int r;
			try {
				r = launcher.launch().cmds(args).envs(env).stdout(aca).pwd(buildFilePath.getParent()).join();
			}
			finally {
				aca.forceEol();
			}
			return r == 0;
		}
		catch (IOException e) {
			Util.displayIOException(e, listener);

			String errorMessage = hudson.tasks.Messages.Ant_ExecFailed();
			if (ai == null && (System.currentTimeMillis() - startTime) < 1000) {
				if (getDescriptor().getInstallations() == null)
				// looks like the user didn't configure any Ant installation
				{
					errorMessage += hudson.tasks.Messages.Ant_GlobalConfigNeeded();
				}
				else
				// There are Ant installations configured but the project didn't pick it
				{
					errorMessage += hudson.tasks.Messages.Ant_ProjectConfigNeeded();
				}
			}
			e.printStackTrace(listener.fatalError(errorMessage));
			return false;
		}
	}

	protected String getOverridedProperties() {
		StringBuilder overridedProperties = new StringBuilder();
		if (StringUtils.isNotBlank(properties)) {
			overridedProperties.append(properties).append('\n');
		}

		overridedProperties.append("test.report").append('=').append(reportDir).append('\n');
		overridedProperties.append("testplan.file").append('=').append(testPlanFile).append('\n');

		int lastFileSep = testPlanFile.lastIndexOf("/");
		if (lastFileSep >= 0) {
			String testName = testPlanFile.substring(lastFileSep + 1, testPlanFile.length() - 4);
			overridedProperties.append("testplan.name").append('=').append(testName).append('\n');
			overridedProperties.append("testrun.id").append('=').append(testName).append('\n');
			overridedProperties.append("testplan.code.path").append('=').append(
					testPlanFile.substring(0, lastFileSep + 1)).append('\n');
		}
		else {

			String testName = testPlanFile.substring(0, testPlanFile.length() - 4);
			overridedProperties.append("testplan.name").append('=').append(testName).append('\n');
			overridedProperties.append("testrun.id").append('=').append(testName).append('\n');
			overridedProperties.append("testplan.code.path").append('=').append('/').append('\n');
		}

		return overridedProperties.toString();
	}

	private static FilePath buildFilePath(FilePath base, String buildFile, String targets) {
		if (buildFile != null) {
			return base.child(buildFile);
		}
		// some users specify the -f option in the targets field, so take that into account as well.
		// see
		String[] tokens = Util.tokenize(targets);
		for (int i = 0; i < tokens.length - 1; i++) {
			String a = tokens[i];
			if (a.equals("-f") || a.equals("-file") || a.equals("-buildfile")) {
				return base.child(tokens[i + 1]);
			}
		}
		return base.child("build.xml");
	}


	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static class DescriptorImpl
			extends BuildStepDescriptor<Builder> {
		@CopyOnWrite
		private volatile ClifInstallation[] installations = new ClifInstallation[0];

		public DescriptorImpl() {
			load();
		}

		protected DescriptorImpl(Class<? extends ClifBuilder> clazz) {
			super(clazz);
		}

		/**
		 * Obtains the {@link ClifInstallation.DescriptorImpl} instance.
		 */
		public ClifInstallation.DescriptorImpl getToolDescriptor() {
			return ToolInstallation.all().get(ClifInstallation.DescriptorImpl.class);
		}

		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getHelpFile() {
			return "/help-clif.html";
		}

		public String getDisplayName() {
			return Messages.Clif_DisplayName();
		}

		public ClifInstallation[] getInstallations() {
			return installations;
		}

		public Ant.AntInstallation[] getAntInstallations() {
			return Hudson.getInstance().getDescriptorByType(Ant.DescriptorImpl.class).getInstallations();
		}

		@Override
		public ClifBuilder newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {
			return (ClifBuilder) req.bindJSON(clazz, formData);
		}

		public void setInstallations(ClifInstallation... clifInstallations) {
			this.installations = clifInstallations;
			save();
		}

		/**
		 * Checks if the ReportDir is valid.
		 */
		public FormValidation doCheckReportDir(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_ReportDirRequired());
			}
			return FormValidation.ok();
		}

		/**
		 * Checks if the TestPlanFile is valid.
		 */
		public FormValidation doCheckTestPlanFile(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_TestPlanFileRequired());
			}
			if (!StringUtils.endsWithIgnoreCase(value, ".ctp")) {
				return FormValidation.error(Messages.Clif_TestPlanFileNotCTP());
			}
			return FormValidation.ok();
		}
	}


}
