package jenkins.model;

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class FakeTest
{

	@Test
	public void canFakeAndResetGlobals()
	{
		assertThat( Jenkins.getInstance() ).isNull();
		Jenkins jenkins = Fake.install();

		assertThat( jenkins ).isNotNull();
		assertThat( Jenkins.getInstance() ).isEqualTo( jenkins );

		Jenkins j = Fake.uninstall();
		assertThat( j ).isNull();
		assertThat( Jenkins.getInstance() ).isNull();
	}


	public void canThenCreateFreestyleProject()
	{
		Jenkins jenkins = Fake.install();
		try
		{
			FreeStyleProject project = new FreeStyleProject(
					(ItemGroup<? extends Item>) jenkins.getItemGroup(),
					"bar"
			);
			assertThat( project.getParent() ).isEqualTo( jenkins );
		}
		finally
		{
			Fake.uninstall();
		}
	}


}
