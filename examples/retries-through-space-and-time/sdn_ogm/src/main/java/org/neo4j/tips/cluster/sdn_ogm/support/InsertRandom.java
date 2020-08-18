package org.neo4j.tips.cluster.sdn_ogm.support;

import java.util.concurrent.ThreadLocalRandom;

public final class InsertRandom {

	public static void delay() {

		try {
			Thread.sleep(ThreadLocalRandom.current().nextLong(1_001));
		} catch (InterruptedException e) {
		}
	}

	private InsertRandom() {
	}
}
