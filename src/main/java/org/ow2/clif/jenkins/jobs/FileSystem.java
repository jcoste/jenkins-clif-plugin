package org.ow2.clif.jenkins.jobs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import com.google.common.collect.Lists;

public class FileSystem {
	private final String dir;

	public FileSystem(String dir) {
	  this.dir = dir;
  }

  public List<String> find(String path, String pattern) {
		return find(dir, path, pattern);
  }

	/**
	 * should behave much like bsd find, in dir
	 * 	cd dir
	 * 	find path -name pattern
	 *
	 * @param dir
	 * @param path
	 * @param pattern
	 * @return
	 */
	@SuppressWarnings("rawtypes")
  static List<String> find(String dir, String path, String pattern) {
  	Collection files = FileUtils.listFiles(
  			new File(dir),
  			new RegexFileFilter(pattern),
  			new RegexFileFilter(path)
  	);

  	String prefix = new File(dir).getAbsolutePath();

  	ArrayList<String> list = Lists.newArrayListWithCapacity(files.size());
  	for (Object o : files) {
  		File f = (File) o;
  		list.add(f.getAbsolutePath().substring(prefix.length() + 1));
  	}
  	return list;
  }

  /**
   * removes dirs matching pattern from dir
   *
   * @param pattern
   * @throws IOException
   */
  public void rm_dir(String pattern) throws IOException {
		List<File> files = Lists.newArrayList();
		File d = new File(dir);
		files = files(d, null, files);
		Pattern re = Pattern.compile(d.getPath() + "/" + pattern);
		for (File f : files) {
			if (re.matcher(f.getPath()).matches()) {
				FileUtils.deleteDirectory(f);
			}
		}
  }

  /**
   * deletes files from dir
   * @param file
   * @throws IOException
   */
  public void rm(String file) throws IOException {
  	FileUtils.forceDelete(new File(dir + "/" + file));
  }

	public static List<File> files(File d, String glob, List<File> acc) {
	  File[] dirs = d.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY);
	  for (File child : dirs) {
	  	acc.add(child);
	  	files(child, glob, acc);
	  }
	  return acc;
  }

	public String dir() {
	  return dir;
  }
}
