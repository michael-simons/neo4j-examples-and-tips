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
package org.neo4j.tips.sdn.causal_cluster.app;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.neo4j.tips.sdn.causal_cluster.domain.Thing;
import org.neo4j.tips.sdn.causal_cluster.domain.ThingService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons
 */
// tag::using-service[]
@RestController
public class ThingController {

	private final ThingService thingService;

	private final AtomicLong sequence;

	// end::using-service[]

	public ThingController(ThingService thingService) {
		this.thingService = thingService;
		this.sequence = new AtomicLong(thingService.getMaxInstance());
	}

	// tag::using-service[]
	@PostMapping("/new")
	public Thing newThing() {

		Thing newThing = this.thingService.newThing(sequence.incrementAndGet());
		List<Thing> readThings = this.thingService
			.findLatestThings(newThing.getSequenceNumber());
		Assert.isTrue(readThings.contains(newThing), "Did not read my own write :(");

		return newThing;
	}
}
// end::using-service[]