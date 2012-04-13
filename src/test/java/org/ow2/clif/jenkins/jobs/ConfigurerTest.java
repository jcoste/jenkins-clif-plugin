package org.ow2.clif.jenkins.jobs;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;

import jenkins.model.Fake;
import jenkins.model.Jenkins;

import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPlugin;
import org.ow2.clif.jenkins.ClifPublisher;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.Installations;

public class ConfigurerTest {

	private Installations installations;
	private Configurer configurer;
	private FreeStyleProject project;
	private Jenkins jenkins;
	private ClifPlugin plugin;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		jenkins = Fake.install();
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

	  plugin = mock(ClifPlugin.class);
		when(jenkins.getPlugin(ClifPlugin.class)).thenReturn(plugin);
		when(plugin.dir()).thenReturn(new File("target/workspaces"));
	}

	@After
	public void tearDown() {
		Fake.uninstall();
	}

	@Test
	public void configureAddsOneClifBuilderToProjectBuilderList()
			throws Exception {
	  configurer.configure(project, null);
	  verify(project.getBuildersList()).add(any(ClifBuilder.class));
  }

	@Test
	public void configurePublisher() throws Exception {
		configurer.configure(project, null);
		verify(project.getPublishersList()).add(any(ClifPublisher.class));
	}

	@Test
	public void configurePrivateWorkspace() throws Exception {
		configurer.configure(project, "examples/http.ctp");
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