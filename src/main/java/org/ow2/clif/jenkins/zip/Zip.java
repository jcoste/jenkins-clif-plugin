package org.ow2.clif.jenkins.zip;

import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip
{
	private ZipInputStream zip;

	public Zip( String file ) throws IOException
	{
		this( new FileInputStream( file ) );
	}

	public Zip( InputStream file )
	{
		zip = new ZipInputStream( file );
	}

	/**
	 * returns entry names of zip, matching given white pattern
	 *
	 * @param white, a regular expression as a string
	 * @return
	 * @throws IOException
	 */
	public List<String> names( String white ) throws IOException
	{
		List<String> list = Lists.newArrayList();
		Pattern pattern = Pattern.compile( white );
		ZipEntry entry;
		while ( (entry = zip.getNextEntry()) != null )
		{
			String entryName = entry.getName();
			if (pattern.matcher( entryName ).matches())
			{
				list.add( entryName );
			}
		}
		return list;
	}
}
