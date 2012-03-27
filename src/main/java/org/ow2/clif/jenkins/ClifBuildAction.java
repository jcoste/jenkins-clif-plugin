package org.ow2.clif.jenkins;

import hudson.model.AbstractBuild;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.chart.*;
import org.ow2.clif.jenkins.model.ClifReport;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

/**
 * Action used for Clif report on build level.
 *
 * @author Julien Coste
 */
public class ClifBuildAction
		extends AbstractClifAction
{
	private final AbstractBuild<?, ?> build;

	private ClifReport report;

	private transient Properties alias;

	public ClifBuildAction( AbstractBuild<?, ?> build, ClifReport report, ClifPublisher publisher,
	                        PrintStream logger )
	{
		this.build = build;
		this.report = report;
		logger.println( "Created Clif results" );
	}

	public void doCallChart( StaplerRequest request, StaplerResponse response )
			throws IOException
	{

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters( params );

		CallChart chart = new CallChart( params.getTestPlan(), params.getBladeId(), params.getLabel() );
		chart.doPng( build.getRootDir(), request, response );
	}

	public void doMovingStatChart( StaplerRequest request, StaplerResponse response )
			throws IOException
	{

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters( params );

		MovingStatChart chart = new MovingStatChart( params.getTestPlan(), params.getBladeId(), params.getLabel(), -1 );
		chart.doPng( build.getRootDir(), request, response );
	}

	public void doFixedSliceNumberDistributionChart( StaplerRequest request, StaplerResponse response )
			throws IOException
	{

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters( params );

		FixedSliceNumberDistributionChart chart =
				new FixedSliceNumberDistributionChart( params.getTestPlan(), params.getBladeId(), params.getLabel(),
				                                       -1 );
		chart.doPng( build.getRootDir(), request, response );
	}

	public void doFixedSliceSizeDistributionChart( StaplerRequest request, StaplerResponse response )
			throws IOException
	{

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters( params );

		FixedSliceSizeDistributionChart chart =
				new FixedSliceSizeDistributionChart( params.getTestPlan(), params.getBladeId(), params.getLabel(), -1 );
		chart.doPng( build.getRootDir(), request, response );
	}

	public void doQuantileDistributionChart( StaplerRequest request, StaplerResponse response )
			throws IOException
	{

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters( params );

		QuantileDistributionChart chart =
				new QuantileDistributionChart( params.getTestPlan(), params.getBladeId(), params.getLabel() );
		chart.doPng( build.getRootDir(), request, response );
	}

	public AbstractBuild<?, ?> getBuild()
	{
		return build;
	}

	public ClifReport getReport()
	{
		return report;
	}

	@Override
	public String getDisplayName()
	{
		return Messages.BuildAction_DisplayName();
	}
}
