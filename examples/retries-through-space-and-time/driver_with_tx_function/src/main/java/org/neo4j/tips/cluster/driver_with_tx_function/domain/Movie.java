package org.neo4j.tips.cluster.driver_with_tx_function.domain;

public class Movie {

	private final String title;

	public Movie(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
