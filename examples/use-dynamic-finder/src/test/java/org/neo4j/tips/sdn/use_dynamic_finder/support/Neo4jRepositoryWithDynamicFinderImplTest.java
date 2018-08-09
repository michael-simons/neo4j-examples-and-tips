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
package org.neo4j.tips.sdn.use_dynamic_finder.support;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.tips.sdn.use_dynamic_finder.domain.Thing;
import org.neo4j.tips.sdn.use_dynamic_finder.domain.ThingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Michael J. Simons
 */
// tag::test-the-new-method[]
@ExtendWith(SpringExtension.class) // <1>
@DataNeo4jTest // <2>
public class Neo4jRepositoryWithDynamicFinderImplTest {

	private final ThingRepository thingRepository;

	@Autowired // <3>
	public Neo4jRepositoryWithDynamicFinderImplTest(
		ThingRepository thingRepository
	) {
		this.thingRepository = thingRepository;
	}

	@Test
	public void findAllByPropertyValueShouldWork() {
		// <4>
		thingRepository.save(new Thing("Thing 1", "This is the 1st thing", 9.99));
		thingRepository.save(new Thing("AnotherThing", "This is a thing, too", 9.99));
		thingRepository.save(new Thing("YetAnotherThing", "This is a thing, too", 9.99));

		assertThat(thingRepository.findAllByPropertyValue("description", "This is a thing, too")) // <5>
			.extracting(Thing::getName)
			.contains("AnotherThing", "YetAnotherThing");
	}
}
// end::test-the-new-method[]
