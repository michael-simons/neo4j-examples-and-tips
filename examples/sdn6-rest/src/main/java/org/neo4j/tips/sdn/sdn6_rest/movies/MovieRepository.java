package org.neo4j.tips.sdn.sdn6_rest.movies;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MovieRepository extends Neo4jRepository<Movie, String> {
}
