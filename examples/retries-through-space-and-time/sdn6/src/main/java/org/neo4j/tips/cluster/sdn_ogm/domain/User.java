package org.neo4j.tips.cluster.sdn_ogm.domain;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node(primaryLabel = "Person")
public class User {

	@Id @GeneratedValue
	private Long id;

	private final String name;

	@Relationship(type = "WATCHED")
	private Map<Movie, WatchedMovie> watchedMovies = new HashMap<>();

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

		var watchedMovie = watchedMovies.computeIfAbsent(movie, k -> new WatchedMovie());
		return watchedMovie.incrementAndGet();
	}
}
