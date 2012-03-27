package org.ow2.clif.jenkins;

import com.google.common.collect.ImmutableList;
import hudson.model.FreeStyleProject;
import jenkins.model.Fake;
import jenkins.model.Jenkins;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.ow2.clif.jenkins.zip.Zip;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ZipImporterTest
{
	private Jenkins jenkins;
	private ZipImporter bar;
	private Zip zip;

	@Before
	public void setup() throws Exception
	{
		jenkins = Fake.install();
		bar = new ZipImporter().tendedBy( jenkins );
		zip = mock( Zip.class );
	}

	@After
	public void reset()
	{
		Fake.uninstall();
	}


	@Test
	public void jobNameIsFileNameWithDirSeparatorReplacedByDash()
			throws Exception
	{
		when( zip.names( eq( bar.whiteList ) ) ).
				thenReturn( ImmutableList.of( "red/tomato.erl" ) );

		bar.createProjectInJenkinsForEachFileInZipMatchingFilter( zip, bar.whiteList );

		ArgumentCaptor<FreeStyleProject> captor =
				ArgumentCaptor.forClass( FreeStyleProject.class );
		verify( jenkins ).putItem( captor.capture() );
		assertThat( captor.getValue().getName() ).isEqualTo( "red-tomato" );
	}

	@Test
	public void createsAsManyJobsAsThereAreWhiteListedNamesInZip()
			throws Exception
	{
		when( zip.names( eq( bar.whiteList ) ) ).
				thenReturn( ImmutableList.of( "grapes", "banana" ) );

		bar.createProjectInJenkinsForEachFileInZipMatchingFilter( zip, bar.whiteList );

		verify( jenkins, times( 2 ) ).putItem( any( FreeStyleProject.class ) );
	}
}
