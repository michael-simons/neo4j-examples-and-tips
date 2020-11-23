package org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness;

import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface WhateverRepository extends Neo4jRepository<Whatever, Long> {
}
