package org.neo4j.tips.ogm.choosing_identifier;

import org.springframework.data.repository.CrudRepository;

public interface PersonWithInternalSurrogateKeyRepository
	extends CrudRepository<PersonWithInternalSurrogateKey, Long> {
}
