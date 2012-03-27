package jenkins.model;

import hudson.PluginManager;

import javax.servlet.ServletContext;
import java.io.File;

import static org.mockito.Mockito.mock;


public class Fake extends Jenkins
{

	public Fake( File root, ServletContext context, PluginManager pluginManager )
			throws Exception
	{
		super( root, context, pluginManager );
	}

	private static JenkinsHolder previous;

	/**
	 * run once in @Before
	 * <p/>
	 * method is fragile : when run twice, then reset will not undo properly
	 *
	 * @return
	 */
	public static Jenkins install()
	{
		previous = Jenkins.HOLDER;
		final Jenkins jenkins = mock( Jenkins.class );
		Jenkins.HOLDER = new JenkinsHolder()
		{
			public Jenkins getInstance()
			{
				return jenkins;
			}
		};
		return jenkins;
	}

	/**
	 * run once in @After
	 *
	 * @return
	 */
	public static Jenkins uninstall()
	{
		Jenkins.HOLDER = previous;
		return previous.getInstance();
	}
}
