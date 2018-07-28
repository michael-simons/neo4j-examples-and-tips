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
package org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot;

import java.time.Year;
import java.util.Optional;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Michael J. Simons
 */
public class YearConverter implements AttributeConverter<Year, Integer> {
	@Override
	public Integer toGraphProperty(Year value) {
		return Optional.ofNullable(value).map(Year::getValue).orElse(null);
	}

	@Override
	public Year toEntityAttribute(Integer value) {
		return Optional.ofNullable(value).map(Year::of).orElse(null);
	}
}
