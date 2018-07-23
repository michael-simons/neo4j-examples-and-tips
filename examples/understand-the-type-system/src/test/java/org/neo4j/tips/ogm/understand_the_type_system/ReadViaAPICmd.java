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

import java.util.function.Function;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael J. Simons
 */
public class ReadViaAPICmd implements Function<Long, Integer> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReadViaAPICmd.class);

	private final GraphDatabaseService graphDatabaseService;

	public ReadViaAPICmd(final GraphDatabaseService graphDatabaseService) {
		this.graphDatabaseService = graphDatabaseService;
	}

	@Override
	public Integer apply(final Long nodeId) {
		try (var transaction = graphDatabaseService.beginTx()) {
			final Node node = graphDatabaseService.getNodeById(nodeId);
			final Object testProperty = node.getProperty(NAME_TEST_PROPERTY);
			final Object sourceProperty = node.getProperty(NAME_SOURCE_PROPERTY);
			transaction.success();

			LOGGER.info("Node with id {} was {}, property has value {} and is of type {}", node.getId(), sourceProperty,
					testProperty, testProperty.getClass());
			return (Integer) testProperty;
		}
	}
}
