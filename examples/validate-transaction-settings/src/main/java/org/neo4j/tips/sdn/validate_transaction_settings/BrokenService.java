/*
 * Copyright (c) 2018 "Neo4j, Inc." <https://neo4j.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.tips.sdn.validate_transaction_settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Michael J. Simons
 */
// tag::broken-service[]
@Service
public class BrokenService {
	private final ThingRepository thingRepository;

	public BrokenService(ThingRepository thingRepository) {
		this.thingRepository = thingRepository;
	}

	@Transactional(readOnly = true)
	public ThingEntity tryToWriteInReadOnlyTx() {
		return this.thingRepository.save(new ThingEntity("A thing")); // <1>
	}
}
// end::broken-service[]
