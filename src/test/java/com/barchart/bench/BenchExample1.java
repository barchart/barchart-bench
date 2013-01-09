/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.bench;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.caliper.Param;
import com.yammer.metrics.core.TimerContext;

/**
 * Example benchmark setup.
 */
public class BenchExample1 extends BenchBase1 {

	@Param
	private volatile int latency;

	protected static List<String> latencyValues() {
		return BenchBase1.latencyList();
	}

	@Param
	private volatile int message;

	protected static List<String> messageValues() {
		return BenchBase1.messageList();
	}

	@Param
	private volatile int duration;

	protected static List<String> durationValues() {
		return BenchBase1.durationList();
	}

	@Override
	protected void setUp() throws Exception {
		log.info("init");

		TrafficControl.delay(latency);

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		TrafficControl.delay(0);

		log.info("done");
	}

	/** Perform measurement upto 3 parameters. */
	public void timeMain(final int reps) throws Exception {

		final AtomicBoolean isOn = new AtomicBoolean(true);

		new Thread() {
			@Override
			public void run() {
				for (int index = 0;; index++) {

					if (!isOn.get()) {
						break;
					}

					/** measure rate */
					measure().rate().mark(100 + index);

					/** measure time */
					final TimerContext time = measure().time().time();
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						log.error("", e);
					}
					time.stop();

					/** measure size */
					measure().size().value(100 + index);

				}
			}
		}.start();

		markWait(duration);

		isOn.set(false);

	}

	public static void main(final String[] args) throws Exception {
		MetricsRunner.execute(BenchExample1.class);
	}

}
