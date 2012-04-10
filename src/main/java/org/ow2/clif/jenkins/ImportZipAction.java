package org.ow2.clif.jenkins;

import com.google.common.collect.Lists;
import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.Workspaces;
import org.ow2.clif.jenkins.jobs.Zip;

import java.io.IOException;
import java.util.List;

import static org.apache.commons.io.FilenameUtils.removeExtension;


@Extension
public class ImportZipAction implements RootAction {
	Configurer clif;
	Jenkins jenkins;
	String whiteList;


	public ImportZipAction() {
		clif = new Configurer();
		with(Jenkins.getInstance());
		whiteList = "(.*)\\.ctp$";
	}

	public String getIconFileName() {
		return "/plugin/jenkins-clif-plugin/images/clif-24x24.png";
	}

	public String getDisplayName() {
		return Messages.ZipImporter_DisplayName();
	}

	public String getUrlName() {
		return "clif";
	}

	@SuppressWarnings("unchecked")
	public void doImport(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException, FileUploadException {

		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory())
				.parseRequest(req);

		Zip zip = new Zip(items.get(0));
		List<FreeStyleProject> projects =
				newProjectForEachFileInZipMatchingFilter(zip, whiteList);
		for (FreeStyleProject project : projects) {
      jenkins.putItem(project);
    }

		zip.extractTo(Workspaces.DEFAULT_LOCATION);
		res.sendRedirect2("/");
	}

	/**
	 *
	 * @param zip
	 * @param filter regular expression as a string
	 * @return
	 * @throws IOException
	 */
	List<FreeStyleProject>
	newProjectForEachFileInZipMatchingFilter(Zip zip, String filter)
			throws IOException, InterruptedException {
		List<FreeStyleProject> projects = Lists.newArrayList();
		for (String fileName : zip.names(filter)) {
			projects.add(clif.configure(newProject(fileName), fileName));
		}
		return projects;
	}

	FreeStyleProject newProject(String fileName)
			throws IOException, InterruptedException {
		FreeStyleProject project = new FreeStyleProject(
				(ItemGroup<? extends Item>) jenkins,
				nameThatProject(fileName)
		);
		return project;
	}

	private String nameThatProject(String fileName) {
		return removeExtension(fileName.replace('/', '-'));
	}

	ImportZipAction with(Jenkins jenkins) {
		this.jenkins = jenkins;
		return this;
	}
}
