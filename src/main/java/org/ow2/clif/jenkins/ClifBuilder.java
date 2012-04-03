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


		ArgumentListBuilder args = new ArgumentListBuilder();
		EnvVars env = build.getEnvironment(listener);

		Ant.AntInstallation antInstallation = getAnt();
		try {
			antInstallation = addAntExecutable(launcher, listener, args, env, antInstallation);
		}
		catch (IllegalStateException ise) {
			listener.fatalError(ise.getMessage());
			return false;
		}


		String clifHome = clifInst.getHome();
		String buildFile = clifHome + File.separator + "build.xml";
		String targets = clifInst.getClifHudsonTarget();

		FilePath buildFilePath = buildFilePath(build.getModuleRoot(), buildFile, targets);
		if (!buildFilePath.exists()) {
			try {
				buildFilePath = tryToFixBuildFilePath(build, buildFile, targets, buildFilePath);
			}
			catch (IllegalStateException ise) {
				listener.fatalError(ise.getMessage());
				return false;
			}
		}

		addAntBuildFileIfNecessary(args, buildFile, buildFilePath);
		addSensitiveVariables(build, args);

		if (clifInst.isProactiveInstallation()) {
			// The absolute path to JENKINS workspace directory
			final String workspaceDir = build.getWorkspace().getRemote() + File.separator;
			// The properties are defined in org.ow2.clif.console.lib.batch.LaunchCmdNGScheduler.MandatoryProperties
			// The values affected to the properties comes from the COMCLif related configuration that
			// can be changed in the JENKINS project (or job) configuration (see the "Invoke Clif" build step configuration)
			// The testPlanFile and reportDir variables are relative to the JENKINS workspace directory
			args.add("-Dtestplan.file=" + workspaceDir + this.testPlanFile);
			args.add("-Dtest.report=" + workspaceDir + this.reportDir);
			args.add("-Dsched.url=" + clifInst.getClifProActiveConfig().getSchedulerURL());
			args.add("-Dsched.creds=" + clifInst.getClifProActiveConfig().getSchedulerCredentialsFile());
			args.add("-Dfork.props=" + clifInst.getClifProActiveConfig().getForkProps());
			// The user specifies the clif home in the general JENKINS configuration
			// (see Manage Jenkins->COMClif->CLIF_HOME)
			args.add("-Dclif.home=" + clifHome);
			final String securityPolicyOption =
					clifHome + File.separator + "etc" + File.separator + "proactive.java.policy";
			args.add("-Djava.security.policy=" + securityPolicyOption);
			args.add("-Dlog4j.configuration=file:///" + clifHome + File.separator + "etc" + File.separator +
					         "proactive-log4j");
		}
		else {
			addOverridedProperties(build, args);
		}
		args.addTokenized(targets.replaceAll("[\t\r\n]+", " "));

		addAntHomeToEnvIfNecessary(env, antInstallation);
		addClifPropsToEnvIfNecessary(env);

		if (!launcher.isUnix()) {
			args = addQuoteToEmptyParameters(args);
		}

		return runAntCommand(build, launcher, listener, args, env, antInstallation, buildFilePath);
	}

	private void addClifPropsToEnvIfNecessary(EnvVars env) {
		if (clifOpts != null) {
			env.put("ANT_OPTS", env.expand(clifOpts));
		}
	}

	private void addAntHomeToEnvIfNecessary(EnvVars env, Ant.AntInstallation antInstallation) {
		if (antInstallation != null) {
			env.put("ANT_HOME", antInstallation.getHome());
		}
	}

	private void addOverridedProperties(AbstractBuild<?, ?> build, ArgumentListBuilder args) throws IOException {
		String overridedProperties = getOverridedProperties();
		VariableResolver<String> vr = build.getBuildVariableResolver();
		args.addKeyValuePairsFromPropertyString("-D", overridedProperties, vr, build.getSensitiveBuildVariables());
	}

	private void addSensitiveVariables(AbstractBuild<?, ?> build, ArgumentListBuilder args) {
		args.addKeyValuePairs("-D", build.getBuildVariables(), build.getSensitiveBuildVariables());
	}

	private FilePath tryToFixBuildFilePath(AbstractBuild<?, ?> build, String buildFile, String targets,
	                                       FilePath buildFilePath) throws IOException, InterruptedException {
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
			throw new IllegalStateException("Unable to find build script at " + buildFilePath);
		}
		return buildFilePath;
	}

	private boolean runAntCommand(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener,
	                              ArgumentListBuilder args, EnvVars env, Ant.AntInstallation antInstallation,
	                              FilePath buildFilePath) throws InterruptedException {
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
			if (antInstallation == null && (System.currentTimeMillis() - startTime) < 1000) {
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

	private ArgumentListBuilder addQuoteToEmptyParameters(ArgumentListBuilder args) {
		args = args.toWindowsCommand();
		// For some reason, ant on windows rejects empty parameters but unix does not.
		// Add quotes for any empty parameter values:
		List<String> newArgs = new ArrayList<String>(args.toList());
		newArgs.set(newArgs.size() - 1,
		            newArgs.get(newArgs.size() - 1).replaceAll("(?<= )(-D[^\" ]+)= ", "$1=\"\" "));
		args = new ArgumentListBuilder(newArgs.toArray(new String[newArgs.size()]));
		return args;
	}

	private void addAntBuildFileIfNecessary(ArgumentListBuilder args, String buildFile, FilePath buildFilePath) {
		if (buildFile != null) {
			args.add("-file", buildFilePath.getName());
		}
	}

	private Ant.AntInstallation addAntExecutable(Launcher launcher, BuildListener listener, ArgumentListBuilder args,
	                                             EnvVars env,
	                                             Ant.AntInstallation antInstallation) throws IOException, InterruptedException {
		if (antInstallation == null) {
			args.add(launcher.isUnix() ? "ant" : "ant.bat");
		}
		else {
			antInstallation = antInstallation.forNode(Computer.currentComputer().getNode(), listener);
			antInstallation = antInstallation.forEnvironment(env);
			String exe = antInstallation.getExecutable(launcher);
			if (exe == null) {
				throw new IllegalStateException(
						hudson.tasks.Messages.Ant_ExecutableNotFound(antInstallation.getName()));
			}
			args.add(exe);
		}
		return antInstallation;
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

		@Override
    @SuppressWarnings("rawtypes")
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getHelpFile() {
			return "/help-clif.html";
		}

		@Override
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
