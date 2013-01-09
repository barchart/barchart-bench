/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.bench;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.caliper.Measurement;
import com.google.caliper.MeasurementSet;
import com.google.caliper.Run;
import com.google.caliper.Scenario;
import com.google.caliper.ScenarioResult;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

/**
 * Caliper measure with Metrics provider.
 * <p>
 * measure up to 3 values: {@link #rate()}, {@link #time()}, {@link #size()}
 */
public class MetricsMeasure {

	private final static Logger log = LoggerFactory
			.getLogger(MetricsMeasure.class);

	/**
	 * Gauge any double value
	 */
	public static class SizeGuage extends Gauge<Double> {

		private volatile Double size = 0.0;

		@Override
		public Double value() {
			return size;
		}

		public void value(final double number) {
			this.size = number;
		}
	}

	/**
	 * Default rate measurement units.
	 */
	private static final Map<String, Integer> RATE_UNIT = new HashMap<String, Integer>();
	static {
		RATE_UNIT.put("Rate  B/s", 1);
		RATE_UNIT.put("Rate KB/s", 1024);
		RATE_UNIT.put("Rate MB/s", 1024 * 1024);
		RATE_UNIT.put("Rate GB/s", 1024 * 1024 * 1024);
	}

	/**
	 * Default time measurement units.
	 */
	private static final Map<String, Integer> TIME_UNIT = new HashMap<String, Integer>();
	static {
		TIME_UNIT.put("Time ns", 1);
		TIME_UNIT.put("Time us", 1000);
		TIME_UNIT.put("Time ms", 1000 * 1000);
		TIME_UNIT.put("Time s ", 1000 * 1000 * 1000);
	}

	/**
	 * Default size measurement units.
	 */
	private static final Map<String, Integer> SIZE_UNIT = new HashMap<String, Integer>();
	static {
		SIZE_UNIT.put("Size  B", 1);
		SIZE_UNIT.put("Size KB", 1024);
		SIZE_UNIT.put("Size MB", 1024 * 1024);
		SIZE_UNIT.put("Size GB", 1024 * 1024 * 1024);
	}

	private final Map<Long, Measurement> rateMap = new HashMap<Long, Measurement>();
	private final Map<Long, Measurement> timeMap = new HashMap<Long, Measurement>();
	private final Map<Long, Measurement> sizeMap = new HashMap<Long, Measurement>();

	private final MetricsRegistry metrics = new MetricsRegistry();

	private final Meter rate = metrics.newMeter(getClass(), "rate", "bytes",
			TimeUnit.SECONDS);

	private final Timer time = metrics.newTimer(getClass(), "time",
			TimeUnit.NANOSECONDS, TimeUnit.SECONDS);

	private final SizeGuage size = new SizeGuage();
	{
		metrics.newGauge(getClass(), "", size);
	}

	/**
	 * Rate meter.
	 */
	public Meter rate() {
		return rate;
	}

	/**
	 * Time meter.
	 */
	public Timer time() {
		return time;
	}

	/**
	 * Size meter.
	 */
	public SizeGuage size() {
		return size;
	}

	/**
	 * Workaround: zero breaks gwt web app.
	 */
	private double filter(final double value) {
		if (value <= 0.0) {
			return 1.0;
		} else {
			return value;
		}
	}

	/**
	 * Perform measurement; convert from metrics into caliper.
	 */
	public void mark() {
		final double rateValue = filter(rate.oneMinuteRate());
		final double timeValue = filter(time.mean());
		final double sizeValue = filter(size.value());
		if (rateValue == 1.0 && timeValue == 1.0 && sizeValue == 1.0) {
			/** ignore complete blank entries */
			return;
		}
		{
			final Measurement mark = new Measurement(RATE_UNIT, rateValue,
					rateValue);
			rateMap.put(System.nanoTime(), mark);
		}
		{
			final Measurement mark = new Measurement(TIME_UNIT, timeValue,
					timeValue);
			timeMap.put(System.nanoTime(), mark);
		}
		{
			final Measurement mark = new Measurement(SIZE_UNIT, sizeValue,
					sizeValue);
			sizeMap.put(System.nanoTime(), mark);
		}
	}

	private final Map<String, String> variables = new HashMap<String, String>();

	/**
	 * Caliper scenario variables.
	 */
	public Map<String, String> variables() {
		return variables;
	}

	private MeasurementSet measurementSet(final Map<Long, Measurement> map) {
		final Measurement[] array = map.values().toArray(new Measurement[0]);
		final MeasurementSet set = new MeasurementSet(array);
		return set;
	}

	/**
	 * Attach this measure to parent caliper run.
	 */
	public void appendTo(final Run run) {

		final Scenario scenario = new Scenario(variables());

		/** display rate as caliper durations */
		final MeasurementSet timeSet = measurementSet(rateMap);
		final String timeLog = null;

		/** display time as caliper instances */
		final MeasurementSet instSet = measurementSet(timeMap);
		final String instLog = null;

		/** display size as caliper memory */
		final MeasurementSet heapSet = measurementSet(sizeMap);
		final String heapLog = null;

		final ScenarioResult scenarioResult = new ScenarioResult(timeSet,
				timeLog, instSet, instLog, heapSet, heapLog);

		final Map<Scenario, ScenarioResult> measurements = run
				.getMeasurements();

		measurements.put(scenario, scenarioResult);
	}

	/**
	 * Terminate metrics resources.
	 */
	public void shutdown() {
		rate.stop();
		time.stop();
		metrics.shutdown();
	}
}
