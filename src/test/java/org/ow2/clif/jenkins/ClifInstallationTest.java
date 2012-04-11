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
		doCheckInstallation(OK, "Cette installation Clif ProActive est valide.");
	}

	@Test
	public void testDoCheckInstallationBadHome() {
		home = new File("");
		doCheckInstallation(ERROR, "Champ obligatoire");

		String fileAsHome = "target/test-classes/org/ow2/clif/jenkins/ClifInstallationTest.class";
		home = new File(fileAsHome);
		doCheckInstallation(ERROR,
		                    fileNameWithOSFileSep(fileAsHome) + " n&#039;est pas un répertoire");

		String badClifInstallation = "target/test-classes/badClifInstallation";
		home = new File(badClifInstallation);
		doCheckInstallation(ERROR,
		                    fileNameWithOSFileSep(badClifInstallation) + " ne semble pas être un répertoire Clif");

		String badProActiveInstallation = "target/test-classes/badProActiveInstallation";
		home = new File(badProActiveInstallation);
		doCheckInstallation(ERROR, "Cette installation Clif ProActive est invalide.");
	}

	@Test
	public void testDoCheckInstallationBadURL() {
		schedulerURL = null;
		doCheckInstallation(ERROR,
		                    "L&#039;URL du scheduler est obligatoire pour une installation Clif ProActive.");

		schedulerURL = " ";
		doCheckInstallation(ERROR,
		                    "L&#039;URL du scheduler est obligatoire pour une installation Clif ProActive.");
	}

	@Test
	public void testDoCheckInstallationBadCredentialsFile() {
		schedulerCredentialsFile = new File("");
		doCheckInstallation(ERROR, "Fichier d&#039;accréditation obligatoire.");

		schedulerCredentialsFile = new File("target/test-classes/unknownFile");
		doCheckInstallation(ERROR, "Fichier d&#039;accréditation non trouvé.");
	}

	private String fileNameWithOSFileSep(final String fileAsHome) {
		return StringUtils.replaceChars(fileAsHome, '/', File.separatorChar);
	}

	private void doCheckInstallation(final FormValidation.Kind expectedKind, final String expectedMessage) {
		final FormValidation res = desc.doCheckInstallation(home, schedulerURL, schedulerCredentialsFile);
		assertThat(res.kind).isEqualTo(expectedKind);
		assertThat(res.getMessage()).isEqualTo(expectedMessage);
	}

}
