package org.neo4j.tips.cluster.sdn_ogm.domain;

import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
public class WatchedMovie {

	private Integer numberOfTimes = 0;

	public Integer getNumberOfTimes() {
		return numberOfTimes;
	}

	public Integer incrementAndGet() {
		return this.numberOfTimes += 1;
	}
}
