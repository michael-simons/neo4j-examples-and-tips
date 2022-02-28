package org.neo4j.tips.sdn.sdn6.movies;

import org.springframework.data.neo4j.repository.Neo4jRepository;

interface MovieRepository extends Neo4jRepository<Movie, String> {
}
