package org.ow2.clif.jenkins.jobs;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	 * cd dir
	 * find path -name pattern
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
			String filename = f.getAbsolutePath().substring(prefix.length() + 1);
			list.add(StringUtils.replaceChars(filename,File.separatorChar, '/'));
		}
		return list;
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
		scanner.setIncludes(new String[]{glob});
		scanner.scan();
		return scanner;
	}

	public String dir() {
		return dir;
	}
}
