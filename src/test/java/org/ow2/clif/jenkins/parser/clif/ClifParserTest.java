package org.ow2.clif.jenkins.parser.clif;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;
import org.ow2.clif.jenkins.chart.ChartConfiguration;
import org.ow2.clif.jenkins.model.ClifReport;
import org.ow2.clif.jenkins.model.TestPlan;
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

		long start = -System.currentTimeMillis();
		File reportDir = new File("src/test/resources/reports");
		File buildDir = new File("target/clif");
		ClifParser parser = new ClifParser(reportDir.getAbsolutePath(), buildDir.getAbsoluteFile());

		ChartConfiguration chartConfig =new ChartConfiguration(600, 1200, 15,50, 2);
		parser.setChartConfiguration(chartConfig);
		parser.enableDataCleanup(2,95);
		parser.addActionAliasPattern("random", ".*dummy.*");

		parser.setGenerateCharts(false);
		// Parse Clif report directory
		ClifReport report = parser.parse(System.out);

		assertNotNull(report);
		assertThat(report.getTestplans()).isNotEmpty().containsExactly(new TestPlan("random", null));

		TestPlan testPlanRead = report.getTestplan("random");
		assertThat(testPlanRead.getAggregatedMeasures()).hasSize(1);
		assertThat(testPlanRead.getAlarms()).isNullOrEmpty();
		assertThat(testPlanRead.getInjectors()).hasSize(1);
		assertThat(testPlanRead.getProbes()).isNullOrEmpty();
		assertThat(testPlanRead.getServers()).hasSize(1);

		start += System.currentTimeMillis();
		System.out.println(start);
	}
}
