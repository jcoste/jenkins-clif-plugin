package org.ow2.clif.jenkins;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleProject;
import jenkins.model.Fake;
import jenkins.model.Jenkins;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.FakeConfigurer;
import org.ow2.clif.jenkins.jobs.FileSystem;
import org.ow2.clif.jenkins.jobs.Zip;

public class PreviewZipActionTest {
	private Jenkins jenkins;
	private PreviewZipAction preview;
	private Zip zip;
	private FileSystem fs;
	private Configurer configurer;
	private ImportZipAction parent;

	@Before
	public void setup() throws Exception {
		jenkins = Fake.install();
		configurer = new FakeConfigurer();

		zip = mock(Zip.class);
		fs = mock(FileSystem.class);
		parent = new ImportZipAction();
		preview = new PreviewZipAction(zip, fs);
		preview.parent = parent;
		preview.clif = configurer;
		preview.jenkins = jenkins;
	}

	@After
	public void reset() {
		Fake.uninstall();
	}

	@Test
	public void jobNameIsDasherizedFileNameWithoutExtension()
			throws Exception {
		FreeStyleProject project = preview.install("red/tomato.erl");
		assertThat(project.getName()).isEqualTo("red-tomato");
	}

	@Test
	public void installedJobReplacesPreviousOne()
			throws Exception {
		FreeStyleProject project = preview.install("red/tomato.erl");
		verify(jenkins).putItem(project);
	}

	@Test
  public void uninstalledJobIsDeleted() throws Exception {
		FreeStyleProject project = mock(FreeStyleProject.class);
		when(jenkins.getItem("red-tomato")).thenReturn(project);
		preview.uninstall("red/tomato.erl");
		verify(project).delete();
  }

	@Test
  public void redirectsToPreviewWhenZipCanNotBeExtractedSafely() throws Exception {
	  when(zip.canBeSafelyExtractedTo(anyString())).thenReturn(false);
	  when(zip.id()).thenReturn("123");
	  StaplerResponse response = mock(StaplerResponse.class);

	  preview.process(response);

	  verify(response).sendRedirect2("previews/123");
	  assertThat(parent.getPreviews(preview.id())).isEqualTo(preview);
  }
}
