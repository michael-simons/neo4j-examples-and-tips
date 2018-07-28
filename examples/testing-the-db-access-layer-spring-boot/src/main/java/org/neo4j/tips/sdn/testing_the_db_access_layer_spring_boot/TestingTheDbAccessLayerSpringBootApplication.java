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
import org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music.AbstractArtist;
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

	private final ArtistRepository<? super AbstractArtist> artistRepository;

	private final AlbumRepository albumRepository;

	private final Session session;

	public TestingTheDbAccessLayerSpringBootApplication(
		ArtistRepository<? super AbstractArtist> artistRepository,
		AlbumRepository albumRepository, Session session) {
		this.artistRepository = artistRepository;
		this.albumRepository = albumRepository;
		this.session = session;
	}

	@Override public void run(String... args) throws Exception {

		final Band queen =
			this.artistRepository.save(new Band("Queen"));
		this.albumRepository.save(new Album(queen, "Queen", Year.of(1973)));
		this.albumRepository.save(new Album(queen, "Queen II", Year.of(1974)));
		this.albumRepository.save(new Album(queen, "Sheer Heart Attack", Year.of(1974)));
		this.albumRepository.save(new Album(queen, "A Night At The Opera", Year.of(1975)));
		this.albumRepository.save(new Album(queen, "A Day At The Races", Year.of(1976)));
		this.albumRepository.save(new Album(queen, "News Of The World", Year.of(1977)));

		final Band blackSabbath =
			this.artistRepository.save(new Band("Black Sabbath"));
		this.albumRepository.save(new Album(blackSabbath, "Black Sabbath", Year.of(1970)));
		this.albumRepository.save(new Album(blackSabbath, "Paranoid", Year.of(1970)));
		this.albumRepository.save(new Album(blackSabbath, "Master Of Reality", Year.of(1971)));
		this.albumRepository.save(new Album(blackSabbath, "Volume 4", Year.of(1972)));
		this.albumRepository.save(new Album(blackSabbath, "Sabbath Bloody Sabbath", Year.of(1973)));
		this.albumRepository.save(new Album(blackSabbath, "Sabotage", Year.of(1975)));

		this.session.query(String.class, "MATCH (album:Album) - [:RELEASED_BY] -> (artist:Artist) WHERE artist.name = $artist RETURN album.name",
			Map.of("artist", "Queen")).forEach(System.out::println);
		System.out.println("---");
		this.session.query(String.class, "MATCH (:Artist {name: $artist}) <- [:RELEASED_BY] - (a:Album) RETURN a.name",
			Map.of("artist", "Queen")).forEach(System.out::println);
	}
}
