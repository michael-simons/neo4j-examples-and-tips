package org.neo4j.tips.cluster.sdn_ogm.domain;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label = "Person")
public class User {

	@Id @GeneratedValue
	private Long id;

	private String name;

	@Relationship(type = "WATCHED")
	private List<WatchedMovie> watchedMovies = new ArrayList<>();

	User() {
	}

	public User(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer watch(Movie movie) {

		var potentiallyWatchedMovie = watchedMovies.stream().filter(wm -> wm.getMovie().equals(movie)).findFirst();
		if (potentiallyWatchedMovie.isPresent()) {
			return potentiallyWatchedMovie.get().incrementAndGet();
		} else {
			var numberOfTimes = 1;
			watchedMovies.add(new WatchedMovie(this, movie, numberOfTimes));
			return numberOfTimes;
		}
	}
}
