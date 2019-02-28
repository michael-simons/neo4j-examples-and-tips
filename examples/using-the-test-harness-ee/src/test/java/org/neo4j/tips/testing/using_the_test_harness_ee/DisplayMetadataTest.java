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
package org.neo4j.tips.testing.using_the_test_harness_ee;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.TransactionConfig;
import org.neo4j.driver.v1.Value;
import org.neo4j.harness.EnterpriseTestServerBuilders;
import org.neo4j.harness.ServerControls;

/**
 * Examples on how to use the Test-Harness.
 *
 * @author Michael J. Simons
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DisplayMetadataTest {
	private static final Config driverConfig = Config.build().withoutEncryption().toConfig();

	private ServerControls embeddedDatabaseServer;

	@BeforeAll
	void initializeNeo4j() {

		this.embeddedDatabaseServer = EnterpriseTestServerBuilders.newInProcessBuilder()
			.withConfig("dbms.security.procedures.unrestricted", "examples.*")
			.withFunction(DisplayMetadata.class)
			.newServer();
	}

	@Test
	void shouldMirrorMeta() {

		Map<String, Object> metaData = new HashMap<>();
		metaData.put("Key 1", "Value 1");
		metaData.put("Key 2", "Value 2");

		try (Driver driver = GraphDatabase.driver(embeddedDatabaseServer.boltURI(), driverConfig);
			Session session = driver.session()) {

			TransactionConfig transactionConfig = TransactionConfig.empty().builder().withMetadata(metaData).build();
			StatementResult result = session.run("RETURN examples.mirrorMeta() as mirroredData", transactionConfig);

			String expectedString = "Key 1 = Value 1, Key 2 = Value 2";
			assertThat(result.stream())
				.hasSize(1)
				.extracting(r -> {
					Value value = r.get("mirroredData");
					return value.isNull() ? null : value.asString();
				})
				.containsExactly(expectedString);
		}
	}

	@AfterAll
	void shutdownNeo4j() {
		this.embeddedDatabaseServer.close();
	}
}
