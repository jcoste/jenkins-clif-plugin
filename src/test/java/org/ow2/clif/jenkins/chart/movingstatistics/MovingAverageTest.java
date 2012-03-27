package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: bvjr5731
 * Date: 26/03/12
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public class MovingAverageTest {
	@Test
	public void testCreateMovingMin() throws Exception {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		AbstractMovingStat ms = new MovingMinStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "min", 3, 3);

		assertNotNull(res);
		assertEquals("Bad size", 7, res.getItemCount());
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedMins = {2, 2, 2, 2, 1, 1, 1};
		for (int i = 0; i < expectedMins.length; i++) {
			assertEquals("Bad min at index " + i, expectedMins[i], res.getY(i));
		}
	}

	@Test
	public void testCreateMovingMax() throws Exception {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		AbstractMovingStat ms = new MovingMaxStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "max", 3, 3);

		assertNotNull(res);
		assertEquals("Bad size", 7, res.getItemCount());
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedMaxs = {5, 5, 5, 6, 6, 6, 4};
		for (int i = 0; i < expectedMaxs.length; i++) {
			assertEquals("Bad max at index " + i, expectedMaxs[i], res.getY(i));
		}
	}

	@Test
	public void testCreateMovingAverage() throws Exception {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		MovingAverageStat ms = new MovingAverageStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "average", 3, 3);

		assertNotNull(res);
		assertEquals("Bad size", 7, res.getItemCount());
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedAverages = {10d / 3, 3d, 3d, 13d / 3, 4d, 11d / 3, 8d / 3};
		for (int i = 0; i < expectedAverages.length; i++) {
			assertEquals("Bad average at index " + i, expectedAverages[i], res.getY(i));
		}
	}

	@Test
	public void testCreateMovingMedian() throws Exception {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		AbstractMovingStat ms = new MovingMedianStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "median", 3, 3);

		assertNotNull(res);
		assertEquals("Bad size", 7, res.getItemCount());
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedMedians = {3, 2, 2, 5, 5, 4, 3};
		for (int i = 0; i < expectedMedians.length; i++) {
			assertEquals("Bad median at index " + i, expectedMedians[i], res.getY(i));
		}
	}


/*	@Test
	public void testCreateMovingMaxExtrem() throws Exception
	{
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries( "data" );
		dataset.addSeries( dataSeries );
		for ( int i = 0; i < 200; i++ )
		{
			dataSeries.add( i * 200, 50 );

		}
		dataSeries.addOrUpdate( 100 * 200, 200 );
		printSeries( dataSeries, "dataSeries" );
		AbstractMovingStat ms = new MovingMaxStat();
		XYSeries res = ms.calculateMovingStat( dataset, 0, "max", 5000, 0 );

		assertNotNull( res );
		assertEquals( "Bad size", 201, res.getItemCount() );
		printSeries( res, "res" );
		System.out.println( res );
		double[] expectedMaxs = {5, 5, 5, 6, 6, 6, 4};
		for ( int i = 0; i < expectedMaxs.length; i++ )
		{
			assertEquals( "Bad max at index " + i, expectedMaxs[i], res.getY( i ) );
		}
	}*/

	private void printSeries(XYSeries res, final String seriesName) {
		for (int i = 0; i < res.getItemCount(); i++) {
			System.out.println(seriesName + "[" + i + "]=(" + res.getX(i) + "," + res.getY(i) + ")");

		}
	}


}
