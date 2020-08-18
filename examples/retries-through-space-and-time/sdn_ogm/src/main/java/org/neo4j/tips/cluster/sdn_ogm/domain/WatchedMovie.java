package org.neo4j.tips.cluster.sdn_ogm.domain;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type = "WATCHED")
public class WatchedMovie {

	@Id @GeneratedValue private Long id;

	@StartNode private User user;

	@EndNode private Movie movie;

	private Integer numberOfTimes = 0;

	WatchedMovie() {
	}

	public WatchedMovie(User user, Movie movie, Integer numberOfTimes) {
		this.user = user;
		this.movie = movie;
		this.numberOfTimes = numberOfTimes;
	}

	public Long getId() {
		return id;
	}

	public Integer getNumberOfTimes() {
		return numberOfTimes;
	}

	public Integer incrementAndGet() {
		return this.numberOfTimes += 1;
	}

	public User getUser() {
		return user;
	}

	public Movie getMovie() {
		return movie;
	}
}
