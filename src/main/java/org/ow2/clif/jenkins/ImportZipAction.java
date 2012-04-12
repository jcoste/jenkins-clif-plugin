package org.ow2.clif.jenkins;

import com.google.common.collect.Maps;
import hudson.Extension;
import hudson.model.RootAction;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.FileSystem;
import org.ow2.clif.jenkins.jobs.Workspaces;
import org.ow2.clif.jenkins.jobs.Zip;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Extension
public class ImportZipAction implements RootAction {
	String pattern;

	private Zip zip;
	final Map<String, PreviewZipAction> previews = Maps.newHashMap();

	public void setZipFilePath(String filePath) {
		this.zip = new Zip(filePath);
	}

	public String getZipFilePath() {
		return zip.getFile().getAbsolutePath();
	}

	public ImportZipAction() {
		pattern = "(.*)\\.ctp$";
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

	public void doImport(StaplerRequest req, StaplerResponse res)
			throws IOException, InterruptedException, FileUploadException {
		zip = new Zip(readZipFile(req));
		new PreviewZipAction(zip, location(), pattern)
				.with(this)
				.process(res);
	}

	public String location() {
		return Workspaces.DEFAULT_LOCATION;
	}

	@SuppressWarnings("unchecked")
	private File readZipFile(StaplerRequest req)
			throws IOException, FileUploadException {
		List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory())
				.parseRequest(req);
		File file = File.createTempFile("zip", null);
		try {
			items.get(0).write(file);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return file;
	}

	public PreviewZipAction getPreviews(String id) {
		return previews.get(id);
	}

	public PreviewZipAction addPreview(PreviewZipAction preview) {
		return previews.put(preview.id(), preview);
	}

	public PreviewZipAction removePreview(String id) {
		return previews.remove(id);
	}
}
