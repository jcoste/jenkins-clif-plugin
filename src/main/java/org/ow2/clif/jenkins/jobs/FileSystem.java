package org.ow2.clif.jenkins.jobs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.DirectoryScanner;

public class FileSystem {
	private final String dir;

	public FileSystem(String dir) {
	  this.dir = dir;
  }

  /**
   * removes dirs matching glob from dir
   *
   * @param glob
   * @throws IOException
   */
  public void rm_rf(String glob) {
  	DirectoryScanner scanner = scan(glob);
		for (String f : scanner.getIncludedDirectories()) {
			_rm_f(f);
		}
		for (String f : scanner.getIncludedFiles()) {
			_rm_f(f);
		}
  }


  public void rm(String glob) {
  	for (String f : scan(glob).getIncludedFiles()) {
			_rm_f(f);
		}
  }


  private void _rm_f(String file) {
  	FileUtils.deleteQuietly(new File(dir + "/" + file));
  }

  private DirectoryScanner scan(String glob) {
	  DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(dir());
		scanner.setIncludes(new String[] {glob});
		scanner.scan();
	  return scanner;
  }

	public String dir() {
	  return dir;
  }
}