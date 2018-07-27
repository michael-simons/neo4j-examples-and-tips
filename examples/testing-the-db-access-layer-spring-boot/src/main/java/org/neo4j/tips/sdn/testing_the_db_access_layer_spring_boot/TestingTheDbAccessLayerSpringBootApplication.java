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
import java.util.Map;

import org.neo4j.ogm.session.Session;
import org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music.Album;
import org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music.AlbumRepository;
import org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music.ArtistRepository;
import org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music.Band;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Michael J. Simons
 */
@SpringBootApplication
public class TestingTheDbAccessLayerSpringBootApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(TestingTheDbAccessLayerSpringBootApplication.class, args).close();
	}

	private final ArtistRepository artistRepository;

	private final AlbumRepository albumRepository;

	private final Session session;

	public TestingTheDbAccessLayerSpringBootApplication(
		ArtistRepository artistRepository,
		AlbumRepository albumRepository, Session session) {
		this.artistRepository = artistRepository;
		this.albumRepository = albumRepository;
		this.session = session;
	}

	@Override public void run(String... args) throws Exception {
		final Band queen =
			(Band)this.artistRepository.<Band>save(new Band("Queen"));

		this.albumRepository.save(new Album(queen, "Queen", Year.of(1973)));
		this.albumRepository.save(new Album(queen, "Queen II", Year.of(1974)));

		this.session.query(String.class, "MATCH (album:Album) <- [:RELEASED_BY] - (artist:Artist) WHERE artist.name = $artist RETURN album.name",
			Map.of("artist", "Queen")).forEach(System.out::println);
	}
}
