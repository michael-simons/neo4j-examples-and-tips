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

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Locale;

import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;
import org.neo4j.values.storable.PointValue;

/**
 * Converts a {@code geometry} attribute of type {@code PointValue[]} to a WKT linestring.
 *
 * @author Michael J. Simons
 */
public class GetGeometry {
	private static final String PROPERTY_GEOMETRY = "geometry";

	@UserFunction("examples.getGeometry")
	@Description("Extracts a WKT representation from a 'geometry' array-property of points.")
	public String apply(@Name("node") Node node) {

		if (!node.hasProperty(PROPERTY_GEOMETRY)) {
			return null;
		}

		PointValue[] geometry = (PointValue[]) node.getProperty(PROPERTY_GEOMETRY);
		return Arrays.stream(geometry)
			.map(PointValue::coordinate)
			.map(c -> String.format(Locale.ENGLISH, "%f %f", c[0], c[1]))
			.collect(joining(",", "LINESTRING (", ")"));
	}
}
