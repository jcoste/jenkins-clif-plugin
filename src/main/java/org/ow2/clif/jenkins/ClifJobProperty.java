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
package org.ow2.clif.jenkins;

import hudson.model.Job;
import hudson.model.JobProperty;

/**
 * Created by IntelliJ IDEA.
 * User: bvjr5731
 * Date: 12/04/12
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class ClifJobProperty extends JobProperty<Job<?, ?>> {

	boolean deleteReport;

	public ClifJobProperty(boolean deleteReport) {
		this.deleteReport = deleteReport;
	}

	public boolean isDeleteReport() {
		return deleteReport;
	}

	public void setDeleteReport(boolean deleteReport) {
		this.deleteReport = deleteReport;
	}
}
