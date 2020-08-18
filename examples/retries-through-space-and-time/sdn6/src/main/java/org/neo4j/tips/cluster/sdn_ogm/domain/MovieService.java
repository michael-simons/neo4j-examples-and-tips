package org.neo4j.tips.cluster.sdn_ogm.domain;

import java.util.Collection;

public interface MovieService {

	Collection<Movie> getAllMovies();

	Integer watchMovie(String userName, String title);
}
