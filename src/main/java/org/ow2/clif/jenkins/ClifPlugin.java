/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2004, 2008 France Telecom R&D
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
package org.ow2.clif.jenkins;

import hudson.Plugin;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 * Entry point for the Clif plugin.
 *
 * @author Julien Coste
 */
public class ClifPlugin extends Plugin {

	static final String ICON_FILE_NAME = "graph.gif";

	static final String DISPLAY_NAME = Messages.Plugin_DisplayName();

	static final String URL = "clif";

	/**
	 * Root directory for storing Clif projects.
	 */
	private String clifRootDir = "clif";


	@Override
	public void configure(StaplerRequest req, JSONObject formData)
			throws IOException, ServletException, Descriptor.FormException {
		clifRootDir = formData.getString("clifRootDir").trim();
		save();
	}

	/**
     * @return The configured Clif root directory.
     */
	public String getClifRootDir() {
		return clifRootDir;
	}

	public void setClifRootDir(String clifRootDir) {
		this.clifRootDir = clifRootDir;
	}

	/**
     * Returns the File object representing the configured clif root directory.
     *
     * @return The configured clif root File object, or <JENKINS_ROOT>/clif  if this configuration has not been set.
     */
    protected File getConfiguredClifRootDir() {
        File rootFile = null;
        if (StringUtils.isNotBlank(clifRootDir)) {
            if (clifRootDir.matches("^(/|\\\\|[a-zA-Z]:).*")) {
                rootFile = new File(clifRootDir);
            } else {
                rootFile = new File(Hudson.getInstance().root.getPath() + "/" + clifRootDir);
            }
        }
	    else {
	        rootFile = new File(Hudson.getInstance().root.getPath() + "/clif" );
        }
        return rootFile;
    }

}
