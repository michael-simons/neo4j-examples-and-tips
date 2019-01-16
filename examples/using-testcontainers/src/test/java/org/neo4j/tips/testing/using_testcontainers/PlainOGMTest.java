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

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Michael J. Simons
 */
@Testcontainers
public class PlainOGMTest {

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
					+ "MERGE (:Thing {name: 'Thing'})"
					+ "MERGE (:Thing {name: 'Thing 2'})"
					+ "MERGE (:Thing {name: 'Thing 3'})"
				));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@BeforeAll
	static void prepareSessionFactory() {

		var ogmConfiguration = new Configuration.Builder()
			.uri(neo4j.getBoltUrl()) // <1>
			.credentials("neo4j", neo4j.getAdminPassword())
			.build();

		sessionFactory = new SessionFactory(ogmConfiguration, "org.neo4j.tips.testing.using_testcontainers.domain");
	}

	@Test
	void someQueryShouldWork() {

		var result = sessionFactory.openSession()
			.query("MATCH (t:Thing) WHERE t.name =~ $name RETURN t", Map.of("name", "Thing \\d"));

		assertThat(result).hasSize(2);
	}

}

