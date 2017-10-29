package org.openstreetmap.osmaxil.util;

public class IdIncrementor {

	private long sequence = 0;

	public IdIncrementor(long initialValue) {
		this.sequence = initialValue;
	}

	public synchronized long getId() {
		return sequence++;
	}

}
