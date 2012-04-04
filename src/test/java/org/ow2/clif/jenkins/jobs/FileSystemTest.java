package org.ow2.clif.jenkins.jobs;


import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileSystemTest {
	private String path;
	private File workspaces;
	private FileSystem fs;

	@Before
	public void setUp() throws Exception {
		path = "target/workspaces";
		workspaces = new File(path);
		FileUtils.forceMkdir(workspaces);
		fs = new FileSystem(path);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(workspaces);
	}

	@Test
	public void findsFilesMatchingRegexp() throws Exception {
		Zip zip = new Zip("src/test/resources/zips/clif-examples-1.zip");
		zip.extractTo(path);

		List<String> plans = fs.find(zip.basedir(), ".*ctp");
		assertThat(plans).containsExactly(
				"examples/dummy.ctp",
				"examples/synchro.ctp"
		);
	}

	File mkdir(String p) throws Exception {
		File f = new File(fs.dir() + "/" + p);
		FileUtils.forceMkdir(f);
		return f;
	}

	File touch(String p) throws Exception {
		File f = new File(fs.dir() + "/" + p);
		FileUtils.touch(f);
		return f;
	}

	@Test
  public void removesDir() throws Exception {
		File f = mkdir("report/synchro_2012-04-11_10h53m40");
		File monster = touch("report/synchro_2012-04-11_10h53m40.ctp");
		touch("synchro.ctp");
		File elvis = mkdir("report/elvis_2012-04-10_17h58m38");

		fs.rm_dir("report/synchro.*");

	  assertThat(f).doesNotExist();
	  assertThat(monster).doesNotExist();
	  assertThat(elvis).exists();
  }

  @Test
  public void scannerTest() throws Exception {
		mkdir("report/synchro_2012-04-11_10h53m40");
		touch("report/synchro_2012-04-11_10h53m40.ctp");
		touch("synchro.ctp");
		mkdir("report/elvis_2012-04-10_17h58m38");

		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(fs.dir());
		scanner.setIncludes(new String[] {"**/synchro*"});
		scanner.scan();
		String[] dirs = scanner.getIncludedDirectories();
		assertThat(dirs).containsOnly("report/synchro_2012-04-11_10h53m40");
		String[] files = scanner.getIncludedFiles();
		assertThat(files).containsOnly("synchro.ctp", "report/synchro_2012-04-11_10h53m40.ctp");
  }

  @Test
  public void stringtest() throws Exception {
	  String s = "examples/synchro.ctp";
	  assertThat(s.replace("examples/", "examples/report/"))
	  	.isEqualTo("examples/report/synchro.ctp");
  }
}