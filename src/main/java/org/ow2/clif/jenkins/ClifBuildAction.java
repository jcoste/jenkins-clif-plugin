package org.ow2.clif.jenkins;

import hudson.model.AbstractBuild;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.chart.*;
import org.ow2.clif.jenkins.model.ClifReport;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Action used for Clif report on build level.
 *
 * @author Julien Coste
 */
public class ClifBuildAction
		extends AbstractClifAction {
	private final AbstractBuild<?, ?> build;

	private final ClifReport report;

	public ClifBuildAction(final AbstractBuild<?, ?> build, final ClifReport report, final ClifPublisher publisher,
	                       final PrintStream logger) {
		this.build = build;
		this.report = report;
		logger.println("Created Clif results");
	}

	public void doCallChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final CallChart chart = new CallChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doMovingStatChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final MovingStatChart chart =
				new MovingStatChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doFixedSliceNumberDistributionChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final FixedSliceNumberDistributionChart chart =
				new FixedSliceNumberDistributionChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null
				);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doFixedSliceSizeDistributionChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final FixedSliceSizeDistributionChart chart =
				new FixedSliceSizeDistributionChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doQuantileDistributionChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final QuantileDistributionChart chart =
				new QuantileDistributionChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public ClifReport getReport() {
		return report;
	}

	@Override
	public String getDisplayName() {
		return Messages.BuildAction_DisplayName();
	}
}
