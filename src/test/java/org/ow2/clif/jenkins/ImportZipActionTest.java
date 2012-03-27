package org.ow2.clif.jenkins;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;
import hudson.model.FreeStyleProject;

import java.util.List;

import jenkins.model.Fake;
import jenkins.model.Jenkins;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.clif.jenkins.helper.Configurer;
import org.ow2.clif.jenkins.helper.FakeConfigurer;
import org.ow2.clif.jenkins.zip.Zip;

import com.google.common.collect.ImmutableList;

public class ImportZipActionTest {
	private Jenkins jenkins;
	private ImportZipAction importer;
	private Zip zip;
	private Configurer configurer;

	@Before
	public void setup() throws Exception {
		jenkins = Fake.install();
		configurer = new FakeConfigurer();
		importer = new ImportZipAction().with(jenkins);
		importer.clif = configurer;
		zip = mock(Zip.class);
	}

	@After
	public void reset() {
		Fake.uninstall();
	}

	@Test
	public void jobNameIsDasherizedFileNameWithoutExtension()
			throws Exception {
		when(zip.names(eq(importer.whiteList))).
				thenReturn(ImmutableList.of("red/tomato.erl"));

		List<FreeStyleProject> projects =
				importer.newProjectForEachFileInZipMatchingFilter(zip, importer.whiteList);
		assertThat(projects).hasSize(1);
		FreeStyleProject project = projects.get(0);
		assertThat(project.getName()).isEqualTo("red-tomato");
	}

	@Test
	public void createsAsManyProjectsAsThereAreWhiteListedNamesInZip()
			throws Exception {
		when(zip.names(eq(importer.whiteList))).
				thenReturn(ImmutableList.of("grapes", "banana"));

		List<FreeStyleProject> projects =
				importer.newProjectForEachFileInZipMatchingFilter(zip, importer.whiteList);

		assertThat(projects).hasSize(2);
	}
}
