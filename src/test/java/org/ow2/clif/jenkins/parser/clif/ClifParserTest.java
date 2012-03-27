package org.ow2.clif.jenkins.parser.clif;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.ow2.clif.jenkins.ClifAlias;
import org.ow2.clif.jenkins.ClifResultConfig;
import org.ow2.clif.jenkins.chart.ChartConfiguration;
import org.ow2.clif.jenkins.model.ClifReport;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: bvjr5731
 * Date: 27/03/12
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */
public class ClifParserTest {
	@Test
	public void testParse() throws Exception {

		File reportDir = new File("src/test/resources/reports");
		File buildDir = new File("target/clif");
		ClifParser parser = new ClifParser(reportDir.getAbsolutePath(), buildDir.getAbsoluteFile());

		ChartConfiguration chartConfig =new ChartConfiguration(600, 1200, 15,50, 5);
		parser.setChartConfiguration(chartConfig);
		parser.enableDataCleanup(2,95);
		parser.addActionAliasPattern("random", ".*dummy.*");

		// Parse Clif report directory
		ClifReport report = parser.parse(System.out);
	}
}
