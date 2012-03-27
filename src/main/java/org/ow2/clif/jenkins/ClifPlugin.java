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

/**
 * Entry point for the Clif plugin.
 *
 * @author Julien Coste
 */
public class ClifPlugin
		extends Plugin {

	static final String ICON_FILE_NAME = "graph.gif";

	static final String DISPLAY_NAME = Messages.Plugin_DisplayName();

	static final String URL = "clif";
}
