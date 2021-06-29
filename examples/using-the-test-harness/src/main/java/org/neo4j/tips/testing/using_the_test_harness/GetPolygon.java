/*
 * Copyright (c) 2021 "Neo4j,"
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

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserAggregationFunction;
import org.neo4j.procedure.UserAggregationResult;
import org.neo4j.procedure.UserAggregationUpdate;
import org.neo4j.values.storable.PointValue;

/**
 * @author Michael J. Simons
 */
public class GetPolygon {

	@UserAggregationFunction("examples.getPolygon")
	@Description("Aggregates the location of things into a polygon")
	public PointsIntoPolygonAggregator getPolygon() {
		return new PointsIntoPolygonAggregator();
	}

	public static class PointsIntoPolygonAggregator {
		private List<PointValue> locations = new ArrayList<>();

		@UserAggregationUpdate
		public void collectLocation(@Name("theNode") Node node) {
			if (node.hasProperty("location")) {
				locations.add((PointValue) node.getProperty("location"));
			}
		}

		@UserAggregationResult
		public String result() {
			if (locations.isEmpty()) {
				return "";
			}
			return Stream.concat(locations.stream(), locations.stream().findFirst().stream())
				.map(PointValue::coordinate)
				.map(c -> String.format(Locale.ENGLISH, "%f %f", c[0], c[1]))
				.collect(joining(",", "POLYGON ((", "))"));
		}
	}
}
