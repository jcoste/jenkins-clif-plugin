package org.ow2.clif.jenkins;

import static org.apache.commons.io.FilenameUtils.removeExtension;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.RootAction;
import hudson.model.FreeStyleProject;

import java.io.IOException;
import java.util.List;

import jenkins.model.Jenkins;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.zip.Zip;

import com.google.common.collect.Lists;


@Extension
public class ZipImporter implements RootAction {
	Jenkins jenkins;
	String whiteList;

	public ZipImporter() {
		tendedBy(Jenkins.getInstance());
		whiteList = "(.*)\\.ctp$";
  }

	public String getIconFileName() {
		return "setting.png";
	}

	public String getDisplayName() {
		return Messages.ZipImporter_DisplayName();
	}

	public String getUrlName() {
		return "bar";
	}

	@SuppressWarnings("unchecked")
  public void doImport(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException, FileUploadException {

		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory())
				.parseRequest(req);

		for (FileItem item : items) {
			createProjectInJenkinsForEachFileInZipMatchingFilter(new Zip(item.getInputStream()), whiteList);
    }

		res.sendRedirect2("/");
	}


	/**
	 *
	 * @param name
	 * @param zip
	 * @param filter regular expression as a string
	 * @return
	 * @throws IOException
	 *
	 * wish I could use a list comprehension!
	 *
	 */
	List<FreeStyleProject>
	createProjectInJenkinsForEachFileInZipMatchingFilter(Zip zip, String filter)
			throws IOException, InterruptedException {
		List<FreeStyleProject> projects = Lists.newArrayList();
		for (String fileName : zip.names(filter)) {
			projects.add(createProjectInJenkins(fileName));
		}
		return projects;
  }

	FreeStyleProject createProjectInJenkins(String fileName)
			throws IOException, InterruptedException {
		FreeStyleProject project = new FreeStyleProject(
				(ItemGroup<? extends Item>)jenkins,
				nameThatProject(fileName)
	  );
		jenkins.putItem(project);
		return project;
  }

	private String nameThatProject(String fileName) {
	  return removeExtension(fileName.replace('/', '-'));
  }

	ZipImporter tendedBy(Jenkins tender) {
	  this.jenkins = tender;
	  return this;
  }

	ZipImporter whiteList(String whiteList) {
		this.whiteList = whiteList;
		return this;
	}
}
