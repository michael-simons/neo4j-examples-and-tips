package org.neo4j.tips.ogm.choosing_identifier;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface PersonWithExternalSurrogateKeyRepository
	extends CrudRepository<PersonWithExternalSurrogateKey, UUID> {
}
