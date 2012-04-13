package org.ow2.clif.jenkins.jobs;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPublisher;

public class ConfigurerTest {

	private Installations installations;
	private Configurer configurer;
	private FreeStyleProject project;
	private File dir;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		installations = mock(Installations.class);
		configurer = new Configurer();
		configurer.installations = installations;

	  project = mock(FreeStyleProject.class);
	  DescribableList<Builder, Descriptor<Builder>> builders =
	  		mock(DescribableList.class);
	  when(project.getBuildersList()).thenReturn(builders);

	  DescribableList<Publisher, Descriptor<Publisher>> publishers =
	  		mock(DescribableList.class);
	  when(project.getPublishersList()).thenReturn(publishers);

	  dir = new File("target/workspaces");
	}

	@Test
	public void configureAddsOneClifBuilderToProjectBuilderList()
			throws Exception {
	  configurer.configure(project, dir, null);
	  verify(project.getBuildersList()).add(any(ClifBuilder.class));
  }

	@Test
	public void configurePublisher() throws Exception {
		configurer.configure(project, dir, null);
		verify(project.getPublishersList()).add(any(ClifPublisher.class));
	}

	@Test
	public void configurePrivateWorkspace() throws Exception {
		configurer.configure(project, dir, "examples/http.ctp");
		verify(project).setCustomWorkspace("target/workspaces/examples");
	}

	@Test
	public void newClifBuilderHasTestPlan() throws Exception {
		ClifBuilder builder = configurer.newClifBuilder("http.ctp");
		assertThat(builder.getTestPlanFile()).isEqualTo("http.ctp");
	}

	@Test
	public void newClifBuilderHasReportDir() throws Exception {
		ClifBuilder builder = configurer.newClifBuilder("http.ctp");
		assertThat(builder.getReportDir()).isEqualTo("report");
	}

	@Test
  public void newClifPublisherHasReportDirectory() throws Exception {
		ClifPublisher publisher = configurer.newClifPublisher("/var/clif/examples");
	  assertThat(publisher.getClifReportDirectory())
	  	.isEqualTo("/var/clif/examples/report");
  }

}