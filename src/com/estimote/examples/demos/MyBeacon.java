package com.estimote.examples.demos;

import com.estimote.sdk.Beacon;

public class MyBeacon{
	Beacon beacon;
	int scan;
	public MyBeacon(Beacon beacon, int scan) {
		super();
		this.beacon = beacon;
		this.scan = scan;
	}
}
