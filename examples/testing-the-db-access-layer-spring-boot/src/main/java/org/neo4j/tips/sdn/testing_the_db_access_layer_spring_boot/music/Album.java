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

import static org.neo4j.ogm.annotation.Relationship.INCOMING;

import java.time.Year;
import java.util.Objects;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Album {
	private Long id;

	@Relationship("RELEASED_BY")
	private AbstractArtist artist;

	private String name;

	private Year releasedIn;

	public Album(AbstractArtist artist, String name, Year releasedIn) {
		this.artist = artist;
		this.name = name;
		this.releasedIn = releasedIn;
	}

	public AbstractArtist getArtist() {
		return artist;
	}

	public String getName() {
		return name;
	}

	public Year getReleasedIn() {
		return releasedIn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Album))
			return false;
		Album album = (Album) o;
		return Objects.equals(artist, album.artist) && Objects.equals(name, album.name)
				&& Objects.equals(releasedIn, album.releasedIn);
	}

	@Override
	public int hashCode() {
		return Objects.hash(artist, name, releasedIn);
	}
}
