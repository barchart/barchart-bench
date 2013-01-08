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

/**
 * Introduce traffic control, such as transfer latency.
 * <p>
 * requires sudo setup for /sbin/tc under current account
 * <p>
 * see http://www.davidverhasselt.com/2008/01/27/passwordless-sudo/
 */
public final class TrafficControl {

    private static final Logger log = LoggerFactory
            .getLogger(TrafficControl.class.getName());

    private TrafficControl() {
    }

    private static final String TC_DELAY = "sudo tc qdisc add dev %s root netem delay %sms limit %s";
    private static final String TC_RESET = "sudo tc qdisc del dev %s root";

    /**
     * verify if traffic control is available
     */
    public static boolean isAvailable() {
        try {
            final int millis = 100;
            final int margin = 20;
            delay(0);
            final long time1 = UnitHelp.ping("localhost");
            delay(millis);
            final long time2 = UnitHelp.ping("localhost");
            delay(0);
            final long time3 = UnitHelp.ping("localhost");
            return time2 >= time1 + millis - margin
                    && time2 >= time3 + millis - margin;
        } catch (final Throwable e) {
            log.debug("", e);
            return false;
        }
    }

    /**
     * Introduce round-trip delay on local host
     * @param time - delay in milliseconds; use zero to remove delay.
     */
    public static void delay(final int time) throws Exception {
        if (time < 0) {
            throw new IllegalArgumentException("negative latency");
        }
        final int delay = time / 2;
        if (delay == 0) {
            UnitHelp.process(String.format(TC_RESET, "lo"));
        } else {
            /** extend packet buffer queue to avoid packet loss due to latency */
            final int limit = 1024 * 1024;
            UnitHelp.process(String.format(TC_RESET, "lo"));
            UnitHelp.process(String.format(TC_DELAY, "lo", delay, limit));
        }
    }

}
