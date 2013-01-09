package com.barchart.bench;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.caliper.CaliperRc;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;

public abstract class BenchBase extends SimpleBenchmark implements Bench {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final int trialCount;
	private final int warmupMillis;
	private final int reportMillis;

	protected BenchBase() {
		this(1);
	}

	protected BenchBase(final int trialCount) {
		this(trialCount, 3000, 1000);
	}

	protected BenchBase(final int trialCount, final int warmupMillis,
			final int reportMillis) {
		this.trialCount = trialCount;
		this.warmupMillis = warmupMillis;
		this.reportMillis = reportMillis;
	}

	public static void fail(final String message) {
		throw new RuntimeException(message);
	}

	@Override
	public void execute() throws Exception {

		final File buildDir = new File(getClass().getResource("/").getPath())
				.getParentFile();

		if (!buildDir.exists()) {
			fail("failed to determine the class path");
		}

		final File reportDir = new File(buildDir.getAbsolutePath()
				+ File.separator + "caliper-reports");

		FileUtils.deleteDirectory(reportDir);

		if (!reportDir.exists()) {
			if (!reportDir.mkdirs()) {
				fail("failed to create the Caliper report directory: "
						+ reportDir.getAbsolutePath());
			}
		}

		final CaliperRc caliperrc = CaliperRc.INSTANCE;
		if (caliperrc.getApiKey() == null || caliperrc.getPostUrl() == null) {
			log.info("#");
			log.info("# Cannot read the configuration properties from ${user.home}/.caliperrc");
			log.info("# Please follow the instructions at:");
			log.info("#    http://code.google.com/p/caliper/wiki/OnlineResults");
			log.info("# to upload and browse the benchmark results.");
			log.info("#");
			fail("missing caliper config");
		}

		new Runner().run( //
				"--marker", "###MARKER###", //
				"--trials", String.valueOf(trialCount), //
				"--warmupMillis", String.valueOf(warmupMillis), //
				"--runMillis", String.valueOf(reportMillis), //
				"--saveResults", reportDir.getAbsolutePath(), //
				"--captureVmLog", getClass().getName() //
				);
	}

}
