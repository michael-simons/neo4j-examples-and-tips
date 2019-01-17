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
package org.neo4j.tips.testing.using_testcontainers;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.tips.testing.using_testcontainers.domain.ThingRepository;
import org.neo4j.tips.testing.using_testcontainers.domain.ThingWithGeometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * @author Michael J. Simons
 */
// tag::sdn-neo4j-testcontainer-setup[]
@Testcontainers
@DataNeo4jTest // <1>
public class SDNTest {

	// end::sdn-neo4j-testcontainer-setup[]

	// tag::copy-plugin[]
	@Container
	private static final Neo4jContainer databaseServer = new Neo4jContainer<>()
		.withCopyFileToContainer(
			MountableFile.forClasspathResource("/geometry-toolbox.jar"),
			"/var/lib/neo4j/plugins/")
		.withClasspathResourceMapping(
			"/test-graph.db",
			"/data/databases/graph.db", BindMode.READ_WRITE);
	// end::copy-plugin[]

	// tag::sdn-neo4j-testcontainer-setup[]
	@TestConfiguration // <2>
	static class Config {

		@Bean // <3>
		public org.neo4j.ogm.config.Configuration configuration() {
			return new Configuration.Builder()
				.uri(databaseServer.getBoltUrl())
				.credentials("neo4j", databaseServer.getAdminPassword())
				.build();
		}
	}

	private final ThingRepository thingRepository;

	@Autowired // <4>
	public SDNTest(ThingRepository thingRepository) {
		this.thingRepository = thingRepository;
	}
	// end::sdn-neo4j-testcontainer-setup[]

	// tag::boring-sdn-test[]
	@Test
	void someQueryShouldWork() {

		var things = thingRepository.findThingByNameMatchesRegex("Thing \\d");
		assertThat(things).hasSize(2);
	}
	// end::boring-sdn-test[]

	// tag::not-boring-sdn-test[]
	@Test
	void customProjectionShouldWork() {

		var expectedWkt
			= "LINESTRING (0.000000 0.000000,"
			+ "10.000000 0.000000,"
			+ "10.000000 10.000000,"
			+ "0.000000 10.000000,"
			+ "0.000000 0.000000)";

		var thingWithGeometry = thingRepository.findThingWithGeometry("A box");
		assertThat(thingWithGeometry).isNotNull()
			.extracting(ThingWithGeometry::getWkt)
			.isEqualTo(expectedWkt);
	}
	// end::not-boring-sdn-test[]

	// tag::sdn-neo4j-testcontainer-setup[]
}
// end::sdn-neo4j-testcontainer-setup[]
