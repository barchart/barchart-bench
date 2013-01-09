package com.barchart.bench;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTrafficControl {

	@Test
	public void available() throws Exception {

		assertTrue(TrafficControl.isAvailable());

	}

}
