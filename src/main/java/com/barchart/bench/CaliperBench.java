/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.bench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.caliper.SimpleBenchmark;

/**
 * Base class for caliper/metrics benchmarks.
 */
public abstract class CaliperBench extends SimpleBenchmark {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private volatile CaliperMeasure measure;

	/**
	 * Caliper metrics wrapper.
	 */
	protected CaliperMeasure measure() {
		return measure;
	}

	/**
	 * Start measurement.
	 */
	@Override
	protected void setUp() throws Exception {
		measure = new CaliperMeasure();
	}

	/**
	 * Finish measurement.
	 */
	@Override
	protected void tearDown() throws Exception {
		measure.shutdown();
	}

	/**
	 * Measure time step and minimum run time.
	 */
	protected long markStep() {
		return 3 * 1000;
	}

	/**
	 * Measure progress while in sleep.
	 */
	protected void markWait(final long time) throws Exception {

		final long timeStart = System.currentTimeMillis();

		while (true) {
			Thread.sleep(markStep());
			measure().mark();
			final long timeFinish = System.currentTimeMillis();
			if (timeFinish - timeStart >= time) {
				System.out.print("+\n");
				return;
			} else {
				System.out.print("-");
				continue;
			}
		}

	}

}
