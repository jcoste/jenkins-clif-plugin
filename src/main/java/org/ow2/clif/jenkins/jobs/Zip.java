package org.ow2.clif.jenkins.jobs;

import static org.apache.commons.lang.StringUtils.chop;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class Zip {
	private final File file;

	public Zip(File file) {
		this.file = file;
	}

	public Zip(String file) {
		this.file = new File(file);
	}

	public File getFile() {
  	return file;
  }

	public String id() {
		return file.getName();
	}

	/**
	 * returns entry names of zip, matching given pattern if provided
	 *
	 * @param pattern, a regular expression as a string
	 * @return
	 * @throws IOException
	 */
	public List<String> entries(String pattern) throws IOException {
		ZipInputStream zip = newStream();

		List<String> list = Lists.newArrayList();
		Pattern re = null;
		if(pattern != null) {
			re = Pattern.compile(pattern);
		}
		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null) {
			String entryName = entry.getName();
			if (re == null || re.matcher(entryName).matches()) {
				list.add(entryName);
			}
		}
		return list;
	}

	/**
	 * syntactic sugar for all entries (entries(null))
	 * @return
	 * @throws IOException
	 */
	public List<String> entries() throws IOException {
		return entries(null);
	}


	/**
	 * @return the first entry name if directory, or empty string otherwise
	 * @throws IOException
	 */
	public String basedir() throws IOException {
		ZipInputStream zip = newStream();
		ZipEntry entry = zip.getNextEntry();
		String name = entry.getName();

		if(entry.isDirectory()) {
			return chop(name);
		}
		int i = name.indexOf('/');
		if (i != -1) {
			return name.substring(0, i);
		}
		return "";
  }

	/**
	 * @param location
	 * @return 	true if all entries do not exists on file system, at location
	 * @throws IOException
	 */
	public boolean canBeSafelyExtractedTo(String location) throws IOException {
		for (String name : entries()) {
	    if (new File(location + "/" + name).exists()) {
	    	return false;
	    }
    }
	  return true;
  }

	/**
	 * unzips to directory
	 *
	 * @param toDir the directory
	 * @throws IOException
	 *
	 */
	public Zip extractTo(String toDir) throws IOException {
		FileUtils.forceMkdir(new File(toDir));
		// many options to do that
		// 1- apache commons compress
		// 	is not a dependency of jenkins
		// 	does not provide such a method
		// 2- ant
		// 	unzips a file (string)
		//  browser does have an InputStream ...
		// 3- hand made :(
		// 	inspired from http://stackoverflow.com/questions/4597821/what-is-the-jodatime-apache-commons-of-zip-unzip-java-utilities
		byte[] buf = new byte[1024];
		ZipEntry zipentry;
		ZipInputStream zip = newStream();
		try {
	    for (zipentry = zip.getNextEntry(); zipentry != null; zipentry = zip.getNextEntry()) {
	    	String entryName = zipentry.getName();

	    	File dest = new File(toDir, entryName);
	    	if (zipentry.isDirectory()) {
	    		dest.mkdirs();
	    	}
	    	else {
	    		dest.getParentFile().mkdirs();
	    		dest.createNewFile();
	    		writeCurrentFile(zip, buf, dest);
	    	}
	    	zip.closeEntry();
	    }
    }
		finally {
			zip.close();
    }
		return this;
	}

	public Zip delete() {
		FileUtils.deleteQuietly(file);
		return this;
	}

	void writeCurrentFile(ZipInputStream zip, byte[] buf, File dest) throws IOException {
	  int n;
	  OutputStream fos = new BufferedOutputStream(new FileOutputStream(dest));
	  try {
	    while ((n = zip.read(buf, 0, buf.length)) > -1) {
	    	fos.write(buf, 0, n);
	    }
    }
	  finally {
	  	fos.close();
    }
  }

	private ZipInputStream newStream() throws FileNotFoundException {
		return new ZipInputStream(new FileInputStream(file));
	}

	/**
	 * diff between zip and fs, for each file matching pattern
	 *
	 * matching file goes to :
	 * - extras if in zip and not in fs
	 * - upgrades if in both
	 * - minus if not in zip and in fs
	 * @param fs
	 * @param pattern
	 * @param minus
	 * @param extras
	 * @param upgrades
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void diff(FileSystem fs,
			String pattern,
			List<String> minus,
			List<String> extras,
      List<String> upgrades) throws IOException {
    List<String> files = fs.find(basedir(), pattern);
    List<String> entries = entries(pattern);

    upgrades.addAll(CollectionUtils.intersection(files, entries));
    minus.addAll(CollectionUtils.subtract(files, entries));
    extras.addAll(CollectionUtils.subtract(entries, files));
  }
}
