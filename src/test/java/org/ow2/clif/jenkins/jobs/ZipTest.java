package org.ow2.clif.jenkins.jobs;

import java.io.File;

import org.apache.tools.ant.types.ZipScanner;
import org.junit.Test;
import org.ow2.clif.jenkins.jobs.Zip;

import static org.fest.assertions.Assertions.assertThat;

public class ZipTest {
	private Zip zip;

	@Test
	public void namesAreZipEntriesFileName() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.entries(".*"))
				.containsExactly("foo.rb", "py.py", "get.rb");

		assertThat(zip.entries(".*")).isEqualTo(zip.entries(null));
		assertThat(zip.entries()).isEqualTo(zip.entries(null));
	}

	@Test
	public void namesCanBeFiltered() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		// OH! "foo.rb" does not match /rb$/
		// it does in rb, erl, js
		assertThat(zip.entries(".*rb$"))
				.containsExactly("foo.rb", "get.rb");
	}

	@Test
	public void namesAreIdempotent() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.entries(".*rb$")).isEqualTo(zip.entries(".*rb$"));
	}

	@Test
	public void namesAreRelativePathFromZip() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.entries("(.*)\\.coffee$"))
				.containsExactly("samples/http/brute.coffee");
	}

	@Test
  public void basedirIsFirstEntryWhenDirectory() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.basedir()).isEqualTo("samples");
  }

	@Test
	public void basedirIsFirstEntryLeadingDirectoryWhenFile() throws Exception {
		zip = new Zip("src/test/resources/zips/clif-examples-1.zip");
		assertThat(zip.basedir()).isEqualTo("examples");
	}

	@Test
	public void dirIsEmptyOtherwise() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.basedir()).isEmpty();
	}

	@Test
	public void antLearningTest() throws Exception {
		ZipScanner zip = new ZipScanner();
		zip.setSrc(new File("src/test/resources/zips/clif-examples-1.zip"));

		String[] files = zip.getIncludedFiles();
		assertThat(files).contains("examples/dummy.ctp");
	}
}
