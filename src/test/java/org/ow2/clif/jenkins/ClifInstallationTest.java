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

import hudson.Util;
import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.localizer.LocaleProvider;

import java.io.File;
import java.util.Locale;

import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static org.fest.assertions.Assertions.assertThat;


public class ClifInstallationTest extends HudsonTestCase {

	private ClifInstallation.DescriptorImpl desc;
	private File home;
	private String schedulerURL;
	private File schedulerCredentialsFile;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		desc = new ClifInstallation.DescriptorImpl();
		home = new File("target/test-classes/goodProActiveInstallation");
		schedulerURL = "rmi://localhost:1099/";
		schedulerCredentialsFile = new File("target/test-classes/goodProActiveInstallation/credentialsFile.cred");
		LocaleProvider.setProvider(new LocaleProvider() {
			@Override
			public Locale get() {
				return Locale.FRENCH;
			}
		});
	}

	@Test
	public void testDoCheckInstallationGoodInstall() {
		doCheckInstallation(OK, Messages.ClifInstallation_ProactiveInstallationValid());
	}

	@Test
	public void testDoCheckInstallationBadHome() {
		home = new File("");
		doCheckInstallation(ERROR, Messages.Clif_HomeRequired());

		String fileAsHome = "target/test-classes/org/ow2/clif/jenkins/ClifInstallationTest.class";
		home = new File(fileAsHome);
		doCheckInstallation(ERROR,Messages.Clif_NotADirectory(home));

		String badClifInstallation = "target/test-classes/badClifInstallation";
		home = new File(badClifInstallation);
		doCheckInstallation(ERROR,Messages.Clif_NotClifDirectory(home));


		String badProActiveInstallation = "target/test-classes/badProActiveInstallation";
		home = new File(badProActiveInstallation);
		doCheckInstallation(ERROR, Messages.ClifInstallation_BadProactiveInstallation());
	}

	@Test
	public void testDoCheckInstallationBadURL() {
		schedulerURL = null;
		doCheckInstallation(ERROR, Messages.ClifInstallation_SchedulerURLMissing());

		schedulerURL = " ";
		doCheckInstallation(ERROR, Messages.ClifInstallation_SchedulerURLMissing());
	}

	@Test
	public void testDoCheckInstallationBadCredentialsFile() {
		schedulerCredentialsFile = new File("");
		doCheckInstallation(ERROR, Messages.ClifInstallation_CredentialsFileMissing());

		schedulerCredentialsFile = new File("target/test-classes/unknownFile");
		doCheckInstallation(ERROR, Messages.ClifInstallation_CredentialsFileNotFound());
	}

	private String fileNameWithOSFileSep(final String fileAsHome) {
		return StringUtils.replaceChars(fileAsHome, '/', File.separatorChar);
	}

	private void doCheckInstallation(final FormValidation.Kind expectedKind, final String expectedMessage) {
		final FormValidation res = desc.doCheckInstallation(home, schedulerURL, schedulerCredentialsFile);
		assertThat(res.kind).isEqualTo(expectedKind);
		assertThat(res.getMessage()).isEqualTo(Util.escape(expectedMessage));
	}

}
