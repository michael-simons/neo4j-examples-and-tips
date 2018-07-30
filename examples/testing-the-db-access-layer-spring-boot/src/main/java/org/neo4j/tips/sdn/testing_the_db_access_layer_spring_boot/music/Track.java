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

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Michael J. Simons
 */
public class Track {
	private Long id;

	@Index(unique = true)
	private String name;

	@Relationship("WRITTEN_BY")
	private Set<AbstractArtist> writtenBy = new HashSet<>();

	@Relationship(value = "FEATURING")
	private Set<SoloArtist> featuring = new HashSet<>();

	public Track(String name) {
		this(name, Set.of());
	}

	public Track(String name, Set<AbstractArtist> writtenBy) {
		this.name = name;
		this.writtenBy = new HashSet<>(writtenBy);
	}

	public Track featuring(final SoloArtist artist) {
		this.featuring.add(artist);
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Track))
			return false;
		Track track = (Track) o;
		return Objects.equals(name, track.name) && Objects.equals(writtenBy, track.writtenBy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, writtenBy);
	}
}
