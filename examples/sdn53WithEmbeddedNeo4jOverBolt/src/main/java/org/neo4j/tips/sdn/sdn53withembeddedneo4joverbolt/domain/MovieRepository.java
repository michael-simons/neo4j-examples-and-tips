package org.neo4j.tips.sdn.sdn53withembeddedneo4joverbolt.domain;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MovieRepository extends Neo4jRepository<Movie, Long> {
}
