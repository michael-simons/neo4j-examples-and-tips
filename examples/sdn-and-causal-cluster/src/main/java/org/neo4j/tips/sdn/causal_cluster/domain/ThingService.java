/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.tips.sdn.causal_cluster.domain;

import java.util.Collections;
import java.util.List;

import org.neo4j.ogm.session.Session;
import org.neo4j.tips.sdn.causal_cluster.domain.Thing;
import org.neo4j.tips.sdn.causal_cluster.domain.ThingRepository;
import org.springframework.data.neo4j.annotation.UseBookmark;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons
 */
// tag::tx-write[]
@Service
public class ThingService {
	// end::tx-write[]
	private final Session session;

	// tag::tx-write[]

	private final ThingRepository thingRepository;

	// end::tx-write[]

	public ThingService(Session session, ThingRepository thingRepository) {
		this.session = session;
		this.thingRepository = thingRepository;
	}

	public long getMaxInstance() {
		return session
			.query(Long.class, "MATCH (t:Thing) RETURN COALESCE(MAX(t.sequenceNumber), -1) AS maxInstance",
				Collections.emptyMap())
			.iterator().next().longValue();
	}

	// tag::tx-write[]
	@Transactional
	public Thing newThing(long i) {
		Thing thing = new Thing(i);
		return this.thingRepository.save(thing);
	}
	// end::tx-write[]

	// tag::tx-read[]
	@Transactional(readOnly = true)
	@UseBookmark
	public List<Thing> findLatestThings(Long sequence) {
		return thingRepository.findAllBySequenceNumberGreaterThanEqual(sequence);
	}
	// end::tx-read[]

	// tag::tx-write[]
}
// end::tx-write[]
