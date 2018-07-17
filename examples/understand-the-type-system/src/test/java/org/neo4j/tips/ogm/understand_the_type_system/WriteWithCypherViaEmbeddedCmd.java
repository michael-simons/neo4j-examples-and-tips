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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.Map;
import java.util.function.Function;

import static org.neo4j.tips.ogm.understand_the_type_system.TypeConversionTest.*;

/**
 * @author Michael J. Simons
 */
public class WriteWithCypherViaEmbeddedCmd implements Function<Integer, Long> {

    private final GraphDatabaseService graphDatabaseService;

    public WriteWithCypherViaEmbeddedCmd(final GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public Long apply(final Integer value) {
        // tag::write-with-cypher-via-embedded[]
        final Map<String, Object> parameters = Map.of(
            NAME_TEST_PROPERTY, value,
            NAME_SOURCE_PROPERTY, "written with Cypher via direct connection to embedded");

        try (var transaction = graphDatabaseService.beginTx()) {
            final Long nodeID = graphDatabaseService.execute(CYPHER_WRITE, parameters)
                    .map(row -> ((Node) row.get("n")).getId()).stream().findFirst().get();
            transaction.success();
            return nodeID;
        }
        // end::write-with-cypher-via-embedded[]
    }
}