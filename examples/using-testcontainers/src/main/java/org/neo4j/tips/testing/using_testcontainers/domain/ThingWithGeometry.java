package org.neo4j.tips.testing.using_testcontainers.domain;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class ThingWithGeometry {
	private String name;

	private String wkt;

	public ThingWithGeometry(String name, String wkt) {
		this.name = name;
		this.wkt = wkt;
	}

	public String getName() {
		return name;
	}

	public String getWkt() {
		return wkt;
	}
}
