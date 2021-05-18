package org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;

public interface MovieRepository extends ReactiveNeo4jRepository<Movie, String> {
}
