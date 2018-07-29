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
package org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music;

import java.util.Objects;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Band extends AbstractArtist {
	private Long id;

	private String name;

	@Relationship("FOUNDED_IN")
	private Country foundedIn;

	public Band(String name) {
		this(name, null); }

	public Band(String name, Country foundedIn) {
		this.name = name;
		this.foundedIn = foundedIn;
	}

	public String getName() {
		return name;
	}

	public Country getFoundedIn() {
		return foundedIn;
	}

	public void setFoundedIn(Country foundedIn) {
		this.foundedIn = foundedIn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Band))
			return false;
		Band band = (Band) o;
		return Objects.equals(name, band.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
