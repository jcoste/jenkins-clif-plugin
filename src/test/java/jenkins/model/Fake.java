package jenkins.model;

import static org.mockito.Mockito.mock;
import hudson.PluginManager;

import java.io.File;

import javax.servlet.ServletContext;


public class Fake extends Jenkins {
	
	public Fake(File root, ServletContext context, PluginManager pluginManager) 
			throws Exception {
	  super(root, context, pluginManager);
  }

	private static JenkinsHolder previous;

	/**
	 * run once in @Before
	 * 
	 * method is fragile : when run twice, then reset will not undo properly
	 * 
	 * @return
	 */
	public static Jenkins install() {
		previous = Jenkins.HOLDER;
		final Jenkins jenkins = mock(Jenkins.class);
		Jenkins.HOLDER = new JenkinsHolder() {
			public Jenkins getInstance() {
				return jenkins;
			}
		};
		return jenkins;
	}
	
	/**
	 * run once in @After
	 * @return
	 */
	public static Jenkins uninstall() {
		Jenkins.HOLDER = previous;
		return previous.getInstance();
	}
}
