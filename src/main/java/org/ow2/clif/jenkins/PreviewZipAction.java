package org.ow2.clif.jenkins;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.FreeStyleProject;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.FileSystem;
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

	private final List<String> uninstalls = Lists.newArrayList();
	private final List<String> upgrades = Lists.newArrayList();
	private final List<String> installs = Lists.newArrayList();

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

	public PreviewZipAction diff() throws IOException {
		zip.diff(fs, pattern, uninstalls, installs, upgrades);
		return this;
	}

	public void doProcess(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException {
		Map<String, Set<String>> actions = parse(req);
		for (Map.Entry<String, Set<String>> e : actions.entrySet()) {
			String entry = e.getKey();
			Set<String> verbs = e.getValue();
			if (verbs.contains("install") || verbs.contains("upgrade")) {
				install(entry);
			}
			if (verbs.contains("uninstall")) {
				uninstall(entry);
				if (verbs.contains("delete")) {
					deleteTestData(entry);
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

	public boolean shouldShow() throws IOException {
		return !zip.canBeSafelyExtractedTo(fs.dir());
  }

	public PreviewZipAction process(StaplerResponse res)
			throws IOException, InterruptedException {

		diff();

		if (shouldShow()) {
		  parent.addPreview(this);
		  res.sendRedirect2("previews/" + zip.id());
		}
		else {
			for (String entry : installs) {
				install(entry);
			}

			zip.extractTo(fs.dir()).delete();

		  parent.removePreview(id());
		  res.sendRedirect2("/");
		}

		return this;
	}


	FreeStyleProject install(String entry)
			throws IOException, InterruptedException {
	  FreeStyleProject project = newProject(entry);
		jenkins().putItem(project);
		return project;
  }

	FreeStyleProject uninstall(String entry)
			throws IOException, InterruptedException {
		FreeStyleProject project =
				(FreeStyleProject)jenkins().getItem(toProjectName(entry));
		if (project != null) {
			project.delete();
		}
		fs.rm(removeExtension(entry) + "*");
		return project;
	}

	void deleteTestData(String entry) throws IOException {
		String s = entry.replace(zip.basedir() + "/", zip.basedir() + "/report/");
		fs.rm_rf(removeExtension(s) + "*");
	}

	private Jenkins jenkins() {
		return jenkins;
	}

	FreeStyleProject newProject(String fileName)
			throws IOException, InterruptedException {
		FreeStyleProject project = new FreeStyleProject(
				(ItemGroup<? extends Item>) jenkins(),
				toProjectName(fileName)
		);
		return clif.configure(project, fileName);
	}

	public static String toProjectName(String fileName) {
		return removeExtension(fileName.replace('/', '-'));
	}

	public String id() {
	  return zip.id();
  }
}
