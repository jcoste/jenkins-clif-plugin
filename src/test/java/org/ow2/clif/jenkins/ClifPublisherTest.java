package org.ow2.clif.jenkins;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ClifPublisherTest {

	@Test
	public void unboundConstructor() throws Exception {
		ClifPublisher publisher = new ClifPublisher("bar");
		assertThat(publisher.getChartHeight()).isEqualTo(600);
		assertThat(publisher.getChartWidth()).isEqualTo(1200);
		assertThat(publisher.getDistributionSliceSize()).isEqualTo(50);

		ClifDataCleanup cleanup = publisher.getDataCleanupConfig();
		assertThat(cleanup.getKeepFactor()).isEqualTo(2);
		assertThat(cleanup.getKeepPercentage()).isEqualTo(95);
	}
}
