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
package org.neo4j.tips.testing.using_the_test_harness;

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.graphdb.Label.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.Config;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Values;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

/**
 * Examples on how to use the Test-Harness.
 *
 * @author Michael J. Simons
 */
// tag::test-harness-setup[]
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // <1>
class GeometryToolboxTest {
	// end::test-harness-setup[]
	private static final Config driverConfig = Config.builder().withoutEncryption().build();

	// tag::test-harness-setup[]
	private Neo4j embeddedDatabaseServer; // <2>

	@BeforeAll // <3>
	void initializeNeo4j() {

		this.embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
			.withProcedure(LocationConversion.class) // <4>
			.withFunction(GetGeometry.class)
			.withFixture("" // <5>
				+ " CREATE (:Place {name: 'Malmö', longitude: 12.995098, latitude: 55.611730})"
				+ " CREATE (:Place {name: 'Aachen', longitude: 6.083736, latitude: 50.776381})"
				+ " CREATE (:Place {name: 'Lost place'})"
				// end::test-harness-setup[]
				+ " CREATE (:Thing {name: 'A box', geometry: ["
				+ "   point({x:  0, y:  0}), "
				+ "   point({x: 10, y:  0}), "
				+ "   point({x: 10, y: 10}), "
				+ "   point({x:  0, y: 10}), "
				+ "   point({x:  0, y:  0})] }"
				+ " )"
				// tag::test-harness-setup[]
			)
			// end::test-harness-setup[]
			.withFixture(graphDatabaseService -> {
				try (var transaction = graphDatabaseService.beginTx()) {
					var node = transaction.createNode(label("Thing"));
					node.setProperty("name", "An empty thing");
					transaction.commit();
				}
				return null;
			})
			// tag::test-harness-setup[]
			.withDisabledServer()
			.build(); // <6>
	}
	// end::test-harness-setup[]

	// tag::test-harness-usage1[]
	@Test
	void shouldConvertLocations() {
		try (var driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
			var session = driver.session()) {

			var result = session.run(""
				+ " MATCH (n:Place) WITH collect(n) AS nodes"
				+ " CALL examples.convertLegacyLocation(nodes) YIELD node"
				+ " RETURN node ORDER BY node.name");

			assertThat(result.stream())
				.hasSize(2)
				.extracting(r -> {
					var node = r.get("node");
					return node.get("location").asPoint();
				})
				.containsExactly(
					Values.point(4326, 6.083736, 50.776381).asPoint(),
					Values.point(4326, 12.995098, 55.611730).asPoint()
				);
		}
	}
	// end::test-harness-usage1[]

	@Test
	void shouldGenerateWkt() {
		try (var driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
			var session = driver.session()) {

			var result = session.run(""
				+ " MATCH (n:Thing)"
				+ " RETURN examples.getGeometry(n) as geometry ORDER BY n.name");

			String expectedWkt = "LINESTRING ("
				+ "0.000000 0.000000,10.000000 0.000000,10.000000 10.000000,0.000000 10.000000,0.000000 0.000000"
				+ ")";
			assertThat(result.stream())
				.hasSize(2)
				.extracting(r -> {
					var geometry = r.get("geometry");
					return geometry.isNull() ? null : geometry.asString();
				})
				.containsExactly(expectedWkt, null);
		}
	}

	@AfterAll
	void shutdownNeo4j() { // <7>
		this.embeddedDatabaseServer.close();
	}

	// tag::test-harness-setup[]
}
// end::test-harness-setup[]
