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

import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Michael J. Simons
 */
@RelationshipEntity("PLAYED_IN")
public class PlayedIn {
	@Id
	@GeneratedValue
	private Long relationshipId;

	@StartNode
	private SoloArtist member;

	@EndNode
	private Band band;

	@Property
	private List<String> yearsActive;

	public PlayedIn(SoloArtist member, Band band, List<String> yearsActive) {
		this.member = member;
		this.band = band;
		this.yearsActive = yearsActive;
	}
}
