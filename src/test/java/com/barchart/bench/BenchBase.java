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
public abstract class BenchBase extends CaliperBench {

	/** introduce network latency */
	protected static List<String> latencyList() {
		if (TrafficControl.isAvailable()) {
			return CaliperRunner.valueList("0,10");
		} else {
			return CaliperRunner.valueList("0");
		}
	}

	/** verify different message sizes */
	protected static List<String> messageList() {
		return CaliperRunner.valueList("500,1500");
	}

	/** benchmark run time per each configuration */
	protected static List<String> durationList() {
		return CaliperRunner.valueList("6000");
	}

}
