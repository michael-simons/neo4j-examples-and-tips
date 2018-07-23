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

import static org.neo4j.tips.ogm.understand_the_type_system.TypeConversionTest.*;

import java.util.Map;
import java.util.function.Function;

import org.neo4j.driver.v1.Driver;

/**
 * @author Michael J. Simons
 */
public class WriteWithCypherViaBoltCmd implements Function<Integer, Long> {

	private final Driver driver;

	public WriteWithCypherViaBoltCmd(final Driver driver) {
		this.driver = driver;
	}

	@Override
	public Long apply(final Integer value) {
		// tag::write-with-cypher-via-bolt[]
		final Map<String, Object> parameters = Map.of(NAME_TEST_PROPERTY, value, NAME_SOURCE_PROPERTY,
				"written with Cypher via Java (Bolt) driver");

		try (var session = driver.session()) {
			return session.writeTransaction(tx -> tx.run(CYPHER_WRITE, parameters).single().get("n").asNode().id());
		}
		// end::write-with-cypher-via-bolt[]
	}
}
