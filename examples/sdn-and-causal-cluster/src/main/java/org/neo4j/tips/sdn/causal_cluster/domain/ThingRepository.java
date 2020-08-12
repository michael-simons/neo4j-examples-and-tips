package org.neo4j.tips.sdn.causal_cluster.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;

/**
 * @author Michael J. Simons
 */
public interface ThingRepository extends Neo4jRepository<Thing, Long> {
	Optional<Thing> findOneBySequenceNumber(Long sequenceNumber);

	List<Thing> findAllBySequenceNumberGreaterThanEqual(long currentSequence);
}
