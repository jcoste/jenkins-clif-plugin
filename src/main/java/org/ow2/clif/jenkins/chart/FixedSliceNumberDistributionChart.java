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
package org.ow2.clif.jenkins.chart;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.ow2.clif.jenkins.parser.clif.Messages;

/**
 * Class used to generate and save a chart attach to a build
 *
 * @author Julien Coste
 */
public class FixedSliceNumberDistributionChart
		extends AbstractChart {

	protected HistogramDataset data = new HistogramDataset();

	public FixedSliceNumberDistributionChart(String testplan, String bladeId, String event,
	                                         ChartConfiguration chartConfiguration) {
		super("FixedSliceNumberDistribution", bladeId, testplan, event, chartConfiguration);

		this.data = new HistogramDataset();
	}

	public void addData(double[] values) {
		if (values != null && values.length > 0) {
			data.addSeries(this.chartId.getEvent(), values, this.chartConfiguration.getDistributionSliceNumber());
		}
	}

	@Override
	protected JFreeChart createChart() {
		JFreeChart chart = ChartFactory.createHistogram(getBasicTitle(),
		                                                Messages.FixedSliceNumberDistributionChart_ResponseTime(),
		                                                Messages.FixedSliceNumberDistributionChart_NumberOfCalls(),
		                                                data, PlotOrientation.VERTICAL, true, true, false);

		if (data.getSeriesCount() != 0) {

			double rangeStart = data.getStartX(0, 0).doubleValue();
			double rangeEnd = data.getEndX(0, data.getItemCount(0) - 1).doubleValue();

			NumberAxis domainAxis = new HistogramAxis(data, 0);
			domainAxis.setAutoRangeIncludesZero(false);
			domainAxis.setVerticalTickLabels(true);
			domainAxis.setTickLabelsVisible(true);
			domainAxis.setTickMarksVisible(true);

			domainAxis.setRange(rangeStart, rangeEnd);
			chart.getXYPlot().setDomainAxis(domainAxis);

			NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
			rangeAxis.setAutoRangeIncludesZero(true);
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		}

		chart.getXYPlot().setRangeGridlinesVisible(true);
		chart.getXYPlot().setDomainGridlinesVisible(false);
		return chart;
	}

	private class HistogramAxis
			extends NumberAxis {

		private static final long serialVersionUID = -1582586041169744515L;

		private final HistogramDataset data;

		private final int serie;

		private final NumberFormat formatter = new DecimalFormat("###.##");

		public HistogramAxis(HistogramDataset data, int serie) {
			super();
			this.data = data;
			this.serie = serie;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		@Override
		protected java.util.List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
			java.util.List result = new ArrayList();

			double currentTickValue;
			for (int i = 0; i < data.getItemCount(serie); i++) {
				currentTickValue = data.getStartX(0, i).doubleValue();
				Tick tick = createTick(edge, currentTickValue);
				result.add(tick);
			}
			// Add last tick
			currentTickValue = data.getEndX(0, data.getItemCount(serie) - 1).doubleValue();
			Tick tick = createTick(edge, currentTickValue);
			result.add(tick);
			return result;
		}

		private Tick createTick(RectangleEdge edge, double currentTickValue) {
			String tickLabel;
			tickLabel = formatter.format(currentTickValue);

			TextAnchor anchor = null;
			TextAnchor rotationAnchor = null;
			double angle = 0.0;
			if (isVerticalTickLabels()) {
				anchor = TextAnchor.CENTER_RIGHT;
				rotationAnchor = TextAnchor.CENTER_RIGHT;
				if (edge == RectangleEdge.TOP) {
					angle = Math.PI / 2.0;
				}
				else {
					angle = -Math.PI / 2.0;
				}
			}
			else {
				if (edge == RectangleEdge.TOP) {
					anchor = TextAnchor.BOTTOM_CENTER;
					rotationAnchor = TextAnchor.BOTTOM_CENTER;
				}
				else {
					anchor = TextAnchor.TOP_CENTER;
					rotationAnchor = TextAnchor.TOP_CENTER;
				}
			}
			return new NumberTick(currentTickValue, tickLabel, anchor, rotationAnchor, angle);
		}
	}
}
