/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.bench;

import java.util.List;

/**
 * Example base benchmark class.
 */
public abstract class BenchBase1 extends MetricsBench {

	/** introduce network latency */
	protected static List<String> latencyList() {
		if (TrafficControl.isAvailable()) {
			return MetricsRunner.valueList("0,10");
		} else {
			return MetricsRunner.valueList("0");
		}
	}

	/** verify different message sizes */
	protected static List<String> messageList() {
		return MetricsRunner.valueList("500,1500");
	}

	/** benchmark run time per each configuration */
	protected static List<String> durationList() {
		return MetricsRunner.valueList("6000");
	}

}
