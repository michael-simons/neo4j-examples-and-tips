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

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

import static org.neo4j.tips.ogm.understand_the_type_system.TypeConversionTest.*;

/**
 * @author Michael J. Simons
 */
public class ReadWithCypherViaBoltCmd implements Function<Long, Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadWithCypherViaBoltCmd.class);

    private final Driver driver;

    public ReadWithCypherViaBoltCmd(final Driver driver) {
        this.driver = driver;
    }

    @Override
    public Long apply(final Long nodeId) {
        try (var session = driver.session()) {
            final Node node = session.readTransaction(tx -> tx.run(CYPHER_READ, Map.of("id", nodeId)).single().get("n").asNode());
            final Value testProperty = node.get(NAME_TEST_PROPERTY);
            LOGGER.info("Node with id {} was {}, property has value {} and is of type {}", node.id(), node.get(NAME_SOURCE_PROPERTY), testProperty, testProperty.getClass());

            // This uses the java standard library representation of the underlying value,
            // that is using a java type that is "sensible" given the underlying type.
            // one could use testProperty.asInt() as well, but that may throws a LossyCoercion-Exception
            return (Long) testProperty.asObject();
        }
    }
}
