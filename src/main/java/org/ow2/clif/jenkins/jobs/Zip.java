package org.ow2.clif.jenkins.jobs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

public class Zip {
	private Reader reader;

	static interface Reader {
		ZipInputStream newStream() throws IOException;
	}

	public Zip(final String file) throws IOException {
		this.reader = new Reader() {
			public ZipInputStream newStream() throws IOException {
	      return new ZipInputStream(new FileInputStream(file));
      }
		};
	}

	public Zip(final FileItem fileItem) {
		this.reader = new Reader() {
			public ZipInputStream newStream() throws IOException {
	      return new ZipInputStream(fileItem.getInputStream());
      }
		};
  }

	/**
	 * returns entry names of zip, matching given white pattern
	 *
	 * @param white, a regular expression as a string
	 * @return
	 * @throws IOException
	 */
	public List<String> names(String white) throws IOException {
		ZipInputStream zip = reader.newStream();

		List<String> list = Lists.newArrayList();
		Pattern pattern = Pattern.compile(white);
		ZipEntry entry;
		while ((entry = zip.getNextEntry()) != null) {
			String entryName = entry.getName();
			if (pattern.matcher(entryName).matches()) {
				list.add(entryName);
			}
		}
		return list;
	}

	/**
	 * unzips to directory
	 *
	 * @param toDir the directory
	 * @throws IOException
	 *
	 */
	public void extractTo(String toDir) throws IOException {
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
		ZipInputStream zip = reader.newStream();
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
}
