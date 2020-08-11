package ac.simons.neo4j.sdn_on_liberty.domain;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface MovieRepository extends Neo4jRepository<Movie, Long> {
}
