package org.ow2.clif.jenkins;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import jenkins.model.Jenkins;
import static com.google.common.collect.Lists.newArrayList;

public class PreviewZipAction {
	Jenkins jenkins;
	Configurer clif;
	ImportZipAction parent;

	private final Zip zip;
	private final String clifWorkspaceDir;
	private final String pattern;

	List<String> uninstalls;
	List<String> upgrades;
	List<String> installs;

	public PreviewZipAction(Zip zip, String clifWorkspaceDir) {
		this(zip, clifWorkspaceDir, null);
	}

	public PreviewZipAction(Zip zip, String clifWorkspaceDir, String pattern) {
		this.zip = zip;
		this.pattern = pattern;
		this.clifWorkspaceDir = clifWorkspaceDir;
		this.clif = new Configurer();
		this.jenkins = Jenkins.getInstance();
	}

	public List<String> getUninstalls() {
		return uninstalls;
	}

	public List<String> getUpgrades() {
		return upgrades;
	}

	public List<String> getInstalls() {
		return installs;
	}

	@SuppressWarnings("unchecked")
	PreviewZipAction diff() throws IOException {
		List<String> newPlans = zip.entries(pattern);
		String dir = zip.basedir();

		List<String> oldPlans = Lists.newArrayList();
		for (Item item : jenkins.getAllItems()) {
			String name = item.getName();
			if (name.startsWith(dir + "-")) {
				oldPlans.add(Jobs.toPlan(name));
			}
		}

		installs = newArrayList();
		uninstalls = newArrayList();
		upgrades = newArrayList();

		installs.addAll(CollectionUtils.subtract(newPlans, oldPlans));
		uninstalls.addAll(CollectionUtils.subtract(oldPlans, newPlans));
		upgrades.addAll(CollectionUtils.intersection(newPlans, oldPlans));

		return this;
	}

	/**
	 * responds to POST /clif/previews/12345
	 *
	 * @param req
	 * @param res
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void doProcess(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException {
		Map<String, Set<String>> actions = parse(req);
		for (Map.Entry<String, Set<String>> e : actions.entrySet()) {
			String plan = e.getKey();
			Set<String> verbs = e.getValue();
			if (verbs.contains("create")) {
				create(plan);
			}
			if (verbs.contains("delete")) {
				if (!verbs.contains("rm")) {
					keepReportsForJob(plan);
				}
				delete(plan);
			}
		}
		zip.extractTo(clifWorkspaceDir).delete();
		res.sendRedirect2("/");
	}

	void keepReportsForJob(String plan) {
		getJob(plan).getProperty(ClifJobProperty.class).setDeleteReport(false);
	}

	// boilerplate
	@SuppressWarnings("rawtypes")
	Map<String, Set<String>> parse(StaplerRequest req) {
		Map<String, Set<String>> results = Maps.newHashMap();
		ParameterParser parser = new ParameterParser();
		for (Enumeration names = req.getParameterNames(); names.hasMoreElements(); ) {
			Map<String, String> p = parser.parse((String) names.nextElement());
			for (Map.Entry<String, String> e : p.entrySet()) {
				Set<String> set = results.get(e.getKey());
				if (set == null) {
					set = Sets.newHashSet(e.getValue());
					results.put(e.getKey(), set);
				}
				else {
					set.add(e.getValue());
				}
			}
		}
		return results;
	}

	public PreviewZipAction with(ImportZipAction parent) {
		this.parent = parent;
		return this;
	}

	public PreviewZipAction process(StaplerResponse res)
			throws IOException, InterruptedException {

		diff();

		parent.addPreview(this);
		res.sendRedirect2("previews/" + id());

		return this;
	}

	FreeStyleProject create(String plan)
			throws IOException, InterruptedException {
		FreeStyleProject project = newProject(plan);
		jenkins().putItem(project);
		jenkins().save();
		return project;
	}

	FreeStyleProject delete(String plan)
			throws IOException, InterruptedException {
		FreeStyleProject job = getJob(plan);
		job.delete();
		return job;
	}

	FreeStyleProject getJob(String plan) {
		return (FreeStyleProject) jenkins().getItem(Jobs.toJob(plan));
	}

	private Jenkins jenkins() {
		return jenkins;
	}

	FreeStyleProject newProject(String plan)
			throws IOException, InterruptedException {
		FreeStyleProject project = Jobs.newJob(jenkins, Jobs.toJob(plan));
		return clif.configure(project, plan);
	}

	public String id() {
		return zip.id();
	}
}
