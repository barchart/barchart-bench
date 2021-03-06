/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.bench;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.caliper.ConfiguredBenchmark;
import com.google.caliper.Environment;
import com.google.caliper.EnvironmentGetter;
import com.google.caliper.Json;
import com.google.caliper.Result;
import com.google.caliper.Run;
import com.google.caliper.Runner;
import com.google.caliper.Scenario;
import com.google.caliper.ScenarioResult;
import com.google.caliper.SimpleBenchmark;
import com.yammer.metrics.core.TimerContext;

/**
 * Custom caliper runner for {@link MetricsBench}.
 */
public final class MetricsRunner {

	private final static Logger log = LoggerFactory
			.getLogger(MetricsRunner.class);

	private MetricsRunner() {
	}

	/**
	 * Parse bench parameters.
	 */
	public static List<String> valueList(final String valueText) {
		return Arrays.asList(valueText.split(","));
	}

	/**
	 * Execute full cycle: warm up, execute and publish benchmark.
	 */
	public static void execute(final Class<? extends MetricsBench> klaz)
			throws Exception {
		Run run;
		run = execute("WARMUP", klaz);
		run = execute("REPORT", klaz);
		final Result result = newResult(run);
		publish(result);
		System.out.println(json(result));
	}

	/**
	 * Execute benchmark for all parameter combinations.
	 */
	public static Run execute(final String name,
			final Class<? extends MetricsBench> klaz) throws Exception {

		final MetricsBench booter = klaz.newInstance();

		final List<Map<String, String>> varsSet = product(booter);

		final Run run = newRun(klaz.getName());

		int index = 0;
		for (final Map<String, String> vars : varsSet) {
			final int done = 100 * index++ / varsSet.size();

			log.info("{} {}% {}", name, done, vars);

			/** call setUp() */
			final ConfiguredBenchmark runner = booter.createBenchmark(vars);

			final MetricsBench bench = (MetricsBench) runner.getBenchmark();
			final MetricsMeasure measure = bench.measure();
			measure.variables().putAll(vars);

			/** call timeXXX() */
			runner.run(0);

			/** call tearDown() */
			runner.close();

			measure.appendTo(run);
		}

		return run;
	}

	/**
	 * Convert caliper result into JSON string.
	 */
	public static String json(final Result result) {
		return Json.getGsonInstance().toJson(result);
	}

	/**
	 * Map signature based on map values.
	 */
	public static String signature(final Map<String, String> map) {
		final StringBuilder text = new StringBuilder();
		for (final String item : map.values()) {
			text.append(String.format("%20s", item));
		}
		return text.toString();
	}

	/**
	 * Generate all parameter combinations for {@link SimpleBenchmark}.
	 */
	public static List<Map<String, String>> product(final SimpleBenchmark bench) {
		final Set<Map<String, String>> collect = new HashSet<Map<String, String>>();
		final Map<String, Set<String>> pending = new TreeMap<String, Set<String>>();
		for (final String name : new TreeSet<String>(bench.parameterNames())) {
			pending.put(name, bench.parameterValues(name));
		}
		final List<Map<String, String>> list = new ArrayList<Map<String, String>>(
				product(collect, pending));
		final Comparator<Map<String, String>> comp = new Comparator<Map<String, String>>() {
			@Override
			public int compare(final Map<String, String> o1,
					final Map<String, String> o2) {
				return signature(o1).compareTo(signature(o2));
			}
		};
		Collections.sort(list, comp);
		return list;
	}

	/**
	 * Calculate ordered Cartesian product of sets.
	 */
	public static Set<Map<String, String>> product(
			final Set<Map<String, String>> collect,
			final Map<String, Set<String>> pending) {

		if (pending.isEmpty()) {
			return collect;
		}

		final Set<Map<String, String>> extract = new HashSet<Map<String, String>>();
		final String key = pending.keySet().iterator().next();
		for (final String value : pending.remove(key)) {
			final Map<String, String> map = new TreeMap<String, String>();
			map.put(key, value);
			extract.add(map);
		}

		if (collect.isEmpty()) {
			collect.addAll(extract);
			return product(collect, pending);
		} else {
			final Set<Map<String, String>> inject = new HashSet<Map<String, String>>();
			for (final Map<String, String> mapExtr : extract) {
				for (final Map<String, String> mapColl : collect) {
					final Map<String, String> mapProd = new TreeMap<String, String>();
					mapProd.putAll(mapExtr);
					mapProd.putAll(mapColl);
					inject.add(mapProd);
				}
			}
			return product(inject, pending);
		}
	}

	/**
	 * Publish result on http://microbenchmarks.appspot.com
	 */
	public static void publish(final Result result) throws Exception {
		final Runner runner = new Runner();
		final Method method = runner.getClass().getDeclaredMethod(
				"postResults", Result.class);
		method.setAccessible(true);
		method.invoke(runner, result);
	}

	/**
	 * Provide new named run instance.
	 */
	public static Run newRun(final String benchmarkName) {
		final Map<Scenario, ScenarioResult> measurements = new HashMap<Scenario, ScenarioResult>();
		final Date executedTimestamp = new Date();
		final Run run = new Run(measurements, benchmarkName, executedTimestamp);
		return run;
	}

	/**
	 * Make new result from run.
	 */
	public static Result newResult(final Run run) {
		final Environment env = new EnvironmentGetter()
				.getEnvironmentSnapshot();
		final Result result = new Result(run, env);
		return result;
	}

	/**
	 * Verify measure publication manually.
	 */
	public static void main(final String[] args) throws Exception {
		final Run run = newRun("test-main");
		for (int param = 0; param < 5; param++) {
			final MetricsMeasure measure = new MetricsMeasure();
			measure.variables().put("param", "" + param);
			for (int step = 0; step < 5; step++) {
				measure.rate().mark(50 + step);
				final TimerContext time = measure.time().time();
				Thread.sleep(15);
				time.stop();
				measure.size().value(50 + step);
				measure.mark();
			}
			measure.appendTo(run);
		}
		final Result result = newResult(run);
		publish(result);
		System.out.println(json(result));
	}

}
