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
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.AuthToken;
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
// tag::minimal-neo4j-testcontainer-setup[]
@Testcontainers // <1>
public class PlainOGMTest {

	@Container // <2>
	private static final Neo4jContainer neo4j = new Neo4jContainer(); // <3>
	// end::minimal-neo4j-testcontainer-setup[]

	// tag::prepare-test-data[]

	static final String TEST_DATA = ""
		+ " MERGE (:Thing {name: 'Thing'  })"
		+ " MERGE (:Thing {name: 'Thing 2'})"
		+ " MERGE (:Thing {name: 'Thing 3'})"
		+ " CREATE (:Thing {name: 'A box', geometry: ["
		+ "   point({x:  0, y:  0}), "
		+ "   point({x: 10, y:  0}), "
		+ "   point({x: 10, y: 10}), "
		+ "   point({x:  0, y: 10}), "
		+ "   point({x:  0, y:  0})] }"
		+ ")";

	@BeforeAll
	static void prepareTestdata() {
		String password = neo4j.getAdminPassword(); // <1>

		AuthToken authToken = AuthTokens.basic("neo4j", password);
		try (var driver = GraphDatabase.driver(neo4j.getBoltUrl(), authToken); // <2>
			var session = driver.session()
		) {
			session.writeTransaction(work -> work.run(TEST_DATA));
		}
	}
	// end::prepare-test-data[]

	// tag::prepare-sessionfactory[]
	private static SessionFactory sessionFactory;

	@BeforeAll
	static void prepareSessionFactory() {

		var ogmConfiguration = new Configuration.Builder()
			.uri(neo4j.getBoltUrl())
			.credentials("neo4j", neo4j.getAdminPassword())
			.build();

		sessionFactory = new SessionFactory(
			ogmConfiguration,
			"org.neo4j.tips.testing.using_testcontainers.domain");
	}
	// end::prepare-sessionfactory[]

	// tag::example-test[]
	@Test
	void someQueryShouldWork() {

		var query = "MATCH (t:Thing) WHERE t.name =~ $name RETURN t";
		var result = sessionFactory.openSession()
			.query(query, Map.of("name", "Thing \\d"));

		assertThat(result).hasSize(2);
	}
	// end::example-test[]

	// tag::minimal-neo4j-testcontainer-setup[]
}
// end::minimal-neo4j-testcontainer-setup[]
