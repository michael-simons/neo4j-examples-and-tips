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

import java.util.List;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;
import org.neo4j.values.storable.CoordinateReferenceSystem;
import org.neo4j.values.storable.PointValue;
import org.neo4j.values.storable.Values;

/**
 * Converts SDN / OGM legacy location property pair to a real point.
 *
 * @author Michael J. Simons
 */
// tag::location-conversion[]
public class LocationConversion {
	// end::location-conversion[]
	private static final String PROPERTY_LOCATION = "location";
	private static final String PROPERTY_LONGITUDE = "longitude";
	private static final String PROPERTY_LATITUDE = "latitude";

	// tag::location-conversion[]
	@Context
	public GraphDatabaseService db;

	@Procedure(name = "examples.convertLegacyLocation", mode = Mode.WRITE)
	public Stream<NodeWrapper> apply(@Name("nodes") List<Node> nodes) {

		return nodes.stream()
			.filter(LocationConversion::hasRequiredProperties)
			.map(LocationConversion::convertPropertiesToLocation)
			.map(NodeWrapper::new);
	}

	static boolean hasRequiredProperties(Node node) {
		return node.hasProperty(PROPERTY_LONGITUDE) && node.hasProperty(PROPERTY_LATITUDE);
	}

	static Node convertPropertiesToLocation(Node node) {

		double lat = (double) node.removeProperty(PROPERTY_LATITUDE);
		double lon = (double) node.removeProperty(PROPERTY_LONGITUDE);
		PointValue location = Values.pointValue(
			CoordinateReferenceSystem.WGS84, lon, lat);

		node.setProperty(PROPERTY_LOCATION, location);

		return node;
	}
	// end::location-conversion[]

	public static class NodeWrapper {
		public final Node node;

		public NodeWrapper(Node node) {
			this.node = node;
		}
	}

	// tag::location-conversion[]
}
// end::location-conversion[]
