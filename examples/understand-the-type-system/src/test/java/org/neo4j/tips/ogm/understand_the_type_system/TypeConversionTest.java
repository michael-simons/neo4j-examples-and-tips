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
package org.neo4j.tips.ogm.understand_the_type_system;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.configuration.BoltConnector;
import org.neo4j.kernel.configuration.Connector;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael J. Simons
 */
@TestInstance(Lifecycle.PER_CLASS)
public class TypeConversionTest {

	static final Logger LOGGER = LoggerFactory.getLogger(TypeConversionTest.class);

	static final String NODE_LABEL = "TestNode";
	static final String NAME_SOURCE_PROPERTY = "source";
	static final String NAME_TEST_PROPERTY = "numericProperty";
	// tag::value-test-property[]
	static final Integer VALUE_TEST_PROPERTY = 42;
	// end::value-test-property[]

	// tag::cypher-write[]
	static final String CYPHER_WRITE = "CREATE (n:TestNode) SET n.numericProperty = $numericProperty, n.source = $source RETURN n";
	// end::cypher-write[]
	// tag::cypher-read[]
	static final String CYPHER_READ = "MATCH (n:TestNode) WHERE id(n) = $id RETURN n";
	// end::cypher-read[]

	private static final String LOCAL_BOLT_URL = "localhost:4711";

	/**
	 * Database storage
	 */
	private File neo4jDb;

	/**
	 * Embedded Neo4j instance which opens a Bolt-port, too.
	 */
	private GraphDatabaseService graphDatabaseService;

	/**
	 * Instance of a Bolt (Java) connected to the embedded instance.
	 */
	private Driver driver;

	@BeforeAll
	void initializeDatabase() throws IOException {
		// tag::using-an-embedded-instance[]
		this.neo4jDb = Files.createTempDirectory("neo4j.db").toFile();

		final BoltConnector bolt = new BoltConnector();
		this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(neo4jDb)
				.setConfig(bolt.type, Connector.ConnectorType.BOLT.name()).setConfig(bolt.enabled, "true")
				.setConfig(bolt.listen_address, LOCAL_BOLT_URL).newGraphDatabase();
		// end::using-an-embedded-instance[]
	}

	@BeforeEach
	void initializeDriverInstance() {
		// Provide a non-embedded connection to this instance to "mock" server mode
		// tag::using-an-embedded-instance[]

		this.driver = GraphDatabase.driver("bolt://" + LOCAL_BOLT_URL, AuthTokens.none());
		// end::using-an-embedded-instance[]
	}

	// tag::writingAndReadingViaDirectApi[]
	@Test
	@DisplayName("Direct API calls should write and read java.lang.Integer as Integer")
	public void writingAndReadingViaDirectApi() {
		assertEquals(VALUE_TEST_PROPERTY, new WriteViaAPICmd(graphDatabaseService)
				.andThen(new ReadViaAPICmd(graphDatabaseService)).apply(VALUE_TEST_PROPERTY),
				"Returned not the same value as written");
	}
	// end::writingAndReadingViaDirectApi[]

	// tag::writingAndReadingWithCypherOverEmbeddedConnection[]
	@Test
	@DisplayName("Cypher over API should write and read java.lang.Integer as Integer")
	public void writingAndReadingWithCypherOverEmbeddedConnection() {
		assertEquals(VALUE_TEST_PROPERTY,
				new WriteWithCypherViaEmbeddedCmd(graphDatabaseService)
						.andThen(new ReadWithCypherViaEmbeddedCmd(graphDatabaseService)).apply(VALUE_TEST_PROPERTY),
				"Returned not the same value as written");
	}
	// end::writingAndReadingWithCypherOverEmbeddedConnection[]

	// tag::writingAndReadingWithCypherOverBoltConnection[]
	@Test
	@DisplayName("Cypher over BOLT should write and read java.lang.Integer as Long")
	public void writingAndReadingWithCypherOverBoltConnection() {
		assertEquals((Long) VALUE_TEST_PROPERTY.longValue(),
				new WriteWithCypherViaBoltCmd(driver).andThen(new ReadWithCypherViaBoltCmd(driver)).apply(VALUE_TEST_PROPERTY),
				"Returned not the same value as written");
	}
	// end::writingAndReadingWithCypherOverBoltConnection[]

	// tag::mixingDifferentAccessMethods[]
	@Test
	@DisplayName("Mixing different access methods has to be handled with care")
	public void mixingDifferentAccessMethods() {
		assertEquals(
				(Long) VALUE_TEST_PROPERTY.longValue(), new WriteViaAPICmd(graphDatabaseService)
						.andThen(new ReadWithCypherViaBoltCmd(driver)).apply(VALUE_TEST_PROPERTY),
				"Bolt should have used the Java-Driver type mapping");

		assertEquals(
				(Long) VALUE_TEST_PROPERTY.longValue(), new WriteWithCypherViaEmbeddedCmd(graphDatabaseService)
						.andThen(new ReadWithCypherViaBoltCmd(driver)).apply(VALUE_TEST_PROPERTY),
				"Bolt should have used the Java-Driver type mapping");

		assertThrows(ClassCastException.class, () -> new WriteWithCypherViaBoltCmd(driver)
				.andThen(new ReadViaAPICmd(graphDatabaseService)).apply(VALUE_TEST_PROPERTY));

		assertThrows(ClassCastException.class, () -> new WriteWithCypherViaBoltCmd(driver)
				.andThen(new ReadWithCypherViaEmbeddedCmd(graphDatabaseService)).apply(VALUE_TEST_PROPERTY));
	}
	// end::mixingDifferentAccessMethods[]

	// tag::ogm-mapping[]
	@Nested
	@DisplayName("OGM applies it's own type mapping...")
	class OGM {
		@Test
		@DisplayName("...over embedded")
		public void overEmbedded() {

			var sessionFactory = new SessionFactory(new EmbeddedDriver(graphDatabaseService),
					this.getClass().getPackage().getName());
			final Session session = sessionFactory.openSession();
			assertAll(session.loadAll(TestNode.class).stream()
					.map(node -> () -> assertEquals(VALUE_TEST_PROPERTY.longValue(), node.getNumericProperty())));
		}

		@Test
		@DisplayName("...over Bolt")
		public void overBolt() {

			var sessionFactory = new SessionFactory(new BoltDriver(driver), this.getClass().getPackage().getName());
			final Session session = sessionFactory.openSession();
			assertAll(session.loadAll(TestNode.class).stream()
					.map(node -> () -> assertEquals(VALUE_TEST_PROPERTY.longValue(), node.getNumericProperty())));
		}
	}
	// end::ogm-mapping[]

	@AfterEach
	void closeDriverInstance() {
		this.driver.close();
	}

	@AfterAll
	void tearDownDatabase() {
		this.graphDatabaseService.shutdown();
		try {
			FileUtils.deleteDirectory(neo4jDb);
		} catch (IOException e) {
			throw new RuntimeException("Failed to delete temporary files in " + neo4jDb.getAbsolutePath(), e);
		}
	}
}
