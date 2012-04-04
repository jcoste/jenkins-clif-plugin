package org.ow2.clif.jenkins.jobs;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ParameterParserTest {
	private ParameterParser parser;

	@Before
	public void setUp() {
		parser = new ParameterParser();
	}

	@Test
  public void parsesHashNotationFor() throws Exception {
		// convention like this one for parameter name
		// http://guides.rubyonrails.org/action_controller_overview.html#hash-and-array-parameters
	  assertThat(
	  	parser.parse("examples/synchro.ctp[uninstall]").get("examples/synchro.ctp")
	  ).isEqualTo("uninstall");
  }

	@Test
  public void isSilentOtherwise() throws Exception {
	  assertThat(
		  	parser.parse("json")
		  ).isEmpty();
  }
}
