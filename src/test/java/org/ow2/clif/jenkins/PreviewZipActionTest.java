package org.ow2.clif.jenkins;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import hudson.model.Item;
import hudson.model.FreeStyleProject;

import java.util.List;

import jenkins.model.Fake;
import jenkins.model.Jenkins;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.FakeConfigurer;
import org.ow2.clif.jenkins.jobs.Installations;
import org.ow2.clif.jenkins.jobs.Zip;

public class PreviewZipActionTest {
	private Jenkins jenkins;
	private PreviewZipAction preview;
	private Zip zip;
	private Configurer configurer;
	private ImportZipAction parent;

	@Before
	public void setUp() throws Exception {
		jenkins = Fake.install();
		configurer = new FakeConfigurer();

		zip = mock(Zip.class);
		parent = new ImportZipAction();
		preview = new PreviewZipAction(zip, null);
		preview.parent = parent;
		preview.clif = configurer;
		preview.jenkins = jenkins;
		preview.installations = mock(Installations.class);
	}

	@After
	public void reset() {
		Fake.uninstall();
	}

	List<Item> jobs(String... names) {
		List<Item> jobs = newArrayList();
		for (String name : names) {
			FreeStyleProject job = job(name);
			jobs.add(job);
		}
		when(jenkins.getAllItems()).thenReturn(jobs);
		return jobs;
	}

	FreeStyleProject job(String name) {
		FreeStyleProject job = mock(FreeStyleProject.class);

		when(job.getName()).thenReturn(name);
		when(jenkins.getItem(name)).thenReturn(job);
		return job;
	}

	@Test
	public void jobNameIsDasherizedFileNameWithoutExtension() throws Exception {
		FreeStyleProject project = preview.create("red/tomato.erl");
		assertThat(project.getName()).isEqualTo("red-tomato");
	}

	@Test
	public void installedJobReplacesPreviousOne() throws Exception {
		FreeStyleProject project = preview.create("red/tomato.erl");
		verify(jenkins).putItem(project);
	}

	@Test
	public void uninstalledJobIsDeleted() throws Exception {
		FreeStyleProject project = job("red-tomato");

		preview.delete("red/tomato.erl");

		verify(project).delete();
	}

	@Test
	public void redirectsToPreview() throws Exception {
		when(zip.id()).thenReturn("123");
		StaplerResponse response = mock(StaplerResponse.class);

		preview.process(response);

		verify(response).sendRedirect2("previews/123");
		assertThat(parent.getPreviews(preview.id())).isEqualTo(preview);
	}

	@Test
	public void diffingZipAgainstJobs() throws Exception {
		jobs("examples-dummy", "examples-synchro", "rebar");
		when(zip.entries(anyString())).thenReturn(
				newArrayList("examples/dummy.ctp", "examples/ftp.ctp")
		);
		when(zip.basedir()).thenReturn("examples");

		preview.diff();

		assertThat(preview.installs).containsOnly("examples/ftp.ctp");
		assertThat(preview.uninstalls).containsOnly("examples/synchro.ctp");
		assertThat(preview.upgrades).containsOnly("examples/dummy.ctp");
	}

}
