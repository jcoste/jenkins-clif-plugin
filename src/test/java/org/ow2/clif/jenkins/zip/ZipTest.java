package org.ow2.clif.jenkins.zip;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ZipTest
{
	private Zip zip;

	@Test
	public void namesAreZipEntriesFileName() throws Exception
	{
		zip = new Zip( "src/test/files/sources.zip" );
		assertThat( zip.names( ".*" ) )
				.containsExactly( "foo.rb", "py.py", "get.rb" );
	}

	@Test
	public void namesCanBeFiltered() throws Exception
	{
		zip = new Zip( "src/test/files/sources.zip" );
		// OH! "rb$" does not match
		// it does in rb, erl, js
		// stupid java!
		assertThat( zip.names( ".*rb$" ) )
				.containsExactly( "foo.rb", "get.rb" );
	}

	@Test
	public void namesAreRelativePathFromZip() throws Exception
	{
		zip = new Zip( "src/test/files/nested.zip" );
		assertThat( zip.names( "(.*)\\.coffee$" ) )
				.containsExactly( "samples/http/brute.coffee" );
	}
}
