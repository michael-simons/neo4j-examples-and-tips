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
package org.neo4j.tips.sdn.causal_cluster.domain;

import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Michael J. Simons
 */
@NodeEntity
public class Thing {
	@Id
	@GeneratedValue
	private Long id;

	@Index(unique = true)
	private Long sequenceNumber;

	private String name;

	Thing() {
	}

	public Thing(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
		this.name = "Thing #" + this.sequenceNumber;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Thing))
			return false;
		Thing thing = (Thing) o;
		return sequenceNumber.equals(thing.sequenceNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sequenceNumber);
	}

	@Override public String toString() {
		return "Thing{" +
			"id=" + id +
			", sequenceNumber=" + sequenceNumber +
			", name='" + name + '\'' +
			'}';
	}
}
