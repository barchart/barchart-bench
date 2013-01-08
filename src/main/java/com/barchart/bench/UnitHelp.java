/**
 * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
 *
 * All rights reserved. Licensed under the OSI BSD License.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */
package com.barchart.bench;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.IntBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test helper.
 */
public final class UnitHelp {

	private static final Logger log = LoggerFactory.getLogger(UnitHelp.class);

	/**
	 * Measure ping time to a host.
	 */
	public static long ping(final String host) throws Exception {
		final String name = System.getProperty("os.name").toLowerCase();

		final String command;
		if (name.contains("linux")) {
			command = "ping -c 1 " + host;
		} else if (name.contains("mac os x")) {
			command = "ping -c 1 " + host;
		} else if (name.contains("windows")) {
			command = "ping -n 1 " + host;
		} else {
			throw new Exception("unknown platform");
		}

		final long timeStart = System.currentTimeMillis();

		process(command);

		final long timeFinish = System.currentTimeMillis();

		final long timeDiff = timeFinish - timeStart;

		return timeDiff;
	}

	/**
	 * Invoke external process and wait for completion.
	 */
	public static void process(final String command) throws Exception {
		final ProcessBuilder builder = new ProcessBuilder(command.split("\\s+"));
		final Process process = builder.start();
		process.waitFor();
	}

	/**
	 * @return newly allocated address or null for failure
	 */
	public static synchronized InetSocketAddress findLocalAddress(
			final String host) {
		ServerSocket socket = null;
		try {
			final InetAddress address = InetAddress.getByName(host);
			socket = new ServerSocket(0, 3, address);
			return (InetSocketAddress) socket.getLocalSocketAddress();
		} catch (final Exception e) {
			log.error("Failed to find addess.");
			return null;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (final Exception e) {
					log.error("Failed to close socket.");
				}
			}
		}
	}

	/**
	 * Find named address on local host.
	 */
	public static InetSocketAddress hostedSocketAddress(final String host)
			throws Exception {
		for (int k = 0; k < 10; k++) {
			final InetSocketAddress address = findLocalAddress(host);
			if (address == null) {
				Thread.sleep(500);
				continue;
			}
			return address;
		}
		throw new Exception("Failed to allocate address.");
	}

	/**
	 * Allocate available local address / port or throw exception.
	 */
	public static InetSocketAddress localSocketAddress() throws Exception {
		return hostedSocketAddress("localhost");
	}

	private UnitHelp() {
	}

}
