package org.ow2.clif.jenkins.jobs;


import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class ZipExtractTest {
	private Zip zip;
	private String path;
	private File workspaces;

	@Before
	public void setUp() throws Exception {
		path = "src/test/resources/workspaces";
		workspaces = new File(path);
		FileUtils.forceMkdir(workspaces);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(workspaces);
	}

	@Test
	public void extractsToDirectoryDeflatesAllZip() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		zip.extractTo(path);
		assertThat(new File(workspaces + "/samples")).isDirectory();
		assertThat(new File(workspaces + "/samples/post.ctp")).isFile();
	}

	@Test
	public void canBeExtractedSafelyWhenNoFileMatchesEntry() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.canBeSafelyExtractedTo(path)).isTrue();
	}

	@Test
	public void canNotBeExtractedSafelyWhenDirExists() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		FileUtils.forceMkdir(new File(path + "/samples"));
		FileUtils.touch(new File(path + "/samples/post.json"));

		assertThat(zip.canBeSafelyExtractedTo(path)).isFalse();
	}
}
