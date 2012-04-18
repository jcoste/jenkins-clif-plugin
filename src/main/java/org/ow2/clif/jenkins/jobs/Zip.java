/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins.jobs;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import com.google.common.collect.Lists;
import static org.apache.commons.lang.StringUtils.chop;

public class Zip {
	private static final Logger logger = Logger.getLogger(Zip.class.getName());
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
		if (pattern != null) {
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
	 *
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

		if (entry.isDirectory()) {
			return chop(name);
		}
		int i = name.indexOf('/');
		if (i != -1) {
			return name.substring(0, i);
		}
		return "";
	}

	public Zip extractTo(String dir) throws IOException {
		return extractTo(new File(dir));
	}

	/**
	 * unzips to directory
	 *
	 * @param dir the directory
	 * @throws IOException
	 */
	public Zip extractTo(File dir) throws IOException {
		FileUtils.forceMkdir(dir);
		if (logger.isLoggable(Level.INFO)) {
			logger.info("extracting " + file + " to " + dir.getAbsolutePath());
		}
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

				File dest = new File(dir, entryName);
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
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("writing " + dest.getAbsolutePath());
		}
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

}
