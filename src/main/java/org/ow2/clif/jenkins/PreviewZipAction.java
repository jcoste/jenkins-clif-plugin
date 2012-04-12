package org.ow2.clif.jenkins;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import static com.google.common.collect.Lists.*;

import hudson.model.Item;
import hudson.model.FreeStyleProject;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jenkins.model.Jenkins;

import org.apache.commons.collections.CollectionUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.FileSystem;
import org.ow2.clif.jenkins.jobs.Jobs;
import org.ow2.clif.jenkins.jobs.ParameterParser;
import org.ow2.clif.jenkins.jobs.Zip;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PreviewZipAction {
	Jenkins jenkins;
	Configurer clif;
	ImportZipAction parent;

	private final Zip zip;
	private final FileSystem fs;
	private final String pattern;

	List<String> uninstalls;
	List<String> upgrades;
	List<String> installs;

	public PreviewZipAction(Zip zip, FileSystem fs) {
		this(zip, fs, null);
	}

	public PreviewZipAction(Zip zip, FileSystem fs, String pattern) {
	  this.zip = zip;
	  this.pattern = pattern;
	  this.fs = fs;
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

		installs =  newArrayList();
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
			if (verbs.contains("update")) {
				rm(plan);
				create(plan);
			}
			if (verbs.contains("delete")) {
				delete(plan);
				if (verbs.contains("rm")) {
					rm(plan);
				}
			}
		}
		zip.extractTo(fs.dir()).delete();
	  res.sendRedirect2("/");
	}

	// boilerplate
	@SuppressWarnings("rawtypes")
  Map<String, Set<String>> parse(StaplerRequest req) {
		Map<String, Set<String>> results = Maps.newHashMap();
		ParameterParser parser = new ParameterParser();
	  for (Enumeration names = req.getParameterNames(); names.hasMoreElements();) {
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
		return project;
  }

	FreeStyleProject delete(String plan)
			throws IOException, InterruptedException {
		FreeStyleProject job =
				(FreeStyleProject)jenkins().getItem(Jobs.toJob(plan));
		if (job != null) {
			job.delete();
		}
		fs.rm(removeExtension(plan) + "*");
		return job;
	}

	void rm(String plan) throws IOException {
		// FIXME grab job and its clif builder
		// and look for report directory attribute
		String s = plan.replace(zip.basedir() + "/", zip.basedir() + "/report/");
		fs.rm_rf(removeExtension(s) + "*");
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
