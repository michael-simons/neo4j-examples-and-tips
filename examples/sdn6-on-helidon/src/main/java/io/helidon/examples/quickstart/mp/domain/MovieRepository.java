package io.helidon.examples.quickstart.mp.domain;

import javax.enterprise.context.ApplicationScoped;

import org.springframework.data.neo4j.repository.Neo4jRepository;

@ApplicationScoped
// IDEA yells here as the repo is an interface and without annotation it is not included in the Jandex file
public interface MovieRepository extends Neo4jRepository<Movie, String> {
}
