package org.ow2.clif.jenkins.jobs;

import org.junit.Test;
import org.ow2.clif.jenkins.jobs.Zip;

import static org.fest.assertions.Assertions.assertThat;

public class ZipTest {
	private Zip zip;

	@Test
	public void namesAreZipEntriesFileName() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.names(".*"))
				.containsExactly("foo.rb", "py.py", "get.rb");
	}

	@Test
	public void namesCanBeFiltered() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		// OH! "foo.rb" does not match /rb$/
		// it does in rb, erl, js
		assertThat(zip.names(".*rb$"))
				.containsExactly("foo.rb", "get.rb");
	}

	@Test
	public void namesAreIdempotent() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.names(".*rb$")).isEqualTo(zip.names(".*rb$"));
	}

	@Test
	public void namesAreRelativePathFromZip() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.names("(.*)\\.coffee$"))
				.containsExactly("samples/http/brute.coffee");
	}
}
