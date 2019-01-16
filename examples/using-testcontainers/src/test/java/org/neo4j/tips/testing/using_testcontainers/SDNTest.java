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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.tips.testing.using_testcontainers.domain.ThingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Michael J. Simons
 */
@Testcontainers
@DataNeo4jTest
public class SDNTest {

	@Container
	private static final Neo4jContainer neo4j = new Neo4jContainer();

	private static SessionFactory sessionFactory;

	@BeforeAll
	static void prepareTestdata() {
		String password = neo4j.getAdminPassword();

		try (var driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", password));
			var session = driver.session()
		) {
			session.writeTransaction(work ->
				work.run(""
					+ "MERGE (:Thing {name: 'Thing',   createdAt: localdatetime('2019-01-01T20:00:00')})"
					+ "MERGE (:Thing {name: 'Thing 2', createdAt: localdatetime('2019-01-02T20:00:00')})"
					+ "MERGE (:Thing {name: 'Thing 3', createdAt: localdatetime('2019-01-03T20:00:00')})"
				));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@TestConfiguration
	static class Config {

		@Bean
		public org.neo4j.ogm.config.Configuration configuration() {
			return new Configuration.Builder()
				.uri(neo4j.getBoltUrl()) // <1>
				.credentials("neo4j", neo4j.getAdminPassword())
				.build();
		}
	}

	private final ThingRepository thingRepository;

	@Autowired
	public SDNTest(ThingRepository thingRepository) {
		this.thingRepository = thingRepository;
	}

	@Test
	void someQueryShouldWork() {

		var things = thingRepository.findThingByNameMatchesRegex("Thing \\d");
		assertThat(things).hasSize(2);
	}
}
