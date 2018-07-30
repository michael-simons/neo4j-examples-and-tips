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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.session.Session;
import org.neo4j.tips.sdn.testing_the_db_access_layer_spring_boot.music.*;
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

	private final CountryRepository countryRepository;

	private final Session session;

	public TestingTheDbAccessLayerSpringBootApplication(
		ArtistRepository<? super AbstractArtist> artistRepository,
		AlbumRepository albumRepository,
		CountryRepository countryRepository,
		Session session) {
		this.artistRepository = artistRepository;
		this.albumRepository = albumRepository;
		this.countryRepository = countryRepository;
		this.session = session;
	}

	@Override public void run(String... args) throws Exception {
		/*
		Stream.concat(
			Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2).stream(),
			Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA3).stream())
			.forEach(c -> {
				if(!countryRepository.findByCode(c).isPresent()) {
					countryRepository.save(new Country(c));
				}
			});
			*/



		final Country greatBritain = countryRepository.findByCode("GB").orElseGet(() -> countryRepository.save(new Country(("GB"))));

		final Band queen =
			this.artistRepository.save(new Band("Queen", greatBritain));
		final SoloArtist brianMay = this.artistRepository.save(new SoloArtist("Brian May")
			.playedIn(queen, Year.of(1970), null));
		final SoloArtist freddieMercury = this.artistRepository.save(new SoloArtist("Freddie Mercury")
			.playedIn(queen, Year.of(1970), Year.of(1991)));
		final SoloArtist rogerTaylor = this.artistRepository.save(new SoloArtist("Roger Taylor")
			.playedIn(queen, Year.of(1970), null));

		final Track keepYourselfAlive = new Track("Keep Yourself Alive", Set.of(brianMay));
		final Track sevenSeasOfRhye = new Track("Seven Seas Of Rhye", Set.of(freddieMercury, brianMay));

		this.albumRepository.save(new Album(queen, "Queen", Year.of(1973))
			.addTrack(keepYourselfAlive, 1, 1)
			.addTrack(sevenSeasOfRhye, 1, 10));
		this.albumRepository.save(new Album(queen, "Queen II", Year.of(1974)).addTrack(sevenSeasOfRhye, 1, 11));
		this.albumRepository.save(new Album(queen, "Sheer Heart Attack", Year.of(1974)));
		this.albumRepository.save(new Album(queen, "A Night At The Opera", Year.of(1975)));
		this.albumRepository.save(new Album(queen, "A Day At The Races", Year.of(1976)));
		this.albumRepository.save(new Album(queen, "News Of The World", Year.of(1977)));

		final Band blackSabbath =
			this.artistRepository.save(new Band("Black Sabbath", greatBritain));
		this.albumRepository.save(new Album(blackSabbath, "Black Sabbath", Year.of(1970)));
		this.albumRepository.save(new Album(blackSabbath, "Paranoid", Year.of(1970)));
		this.albumRepository.save(new Album(blackSabbath, "Master Of Reality", Year.of(1971)));
		this.albumRepository.save(new Album(blackSabbath, "Volume 4", Year.of(1972)));
		this.albumRepository.save(new Album(blackSabbath, "Sabbath Bloody Sabbath", Year.of(1973)));
		this.albumRepository.save(new Album(blackSabbath, "Sabotage", Year.of(1975)));
		this.albumRepository.save(
			new Album(blackSabbath, "Headless Cross", Year.of(1989)).addTrack(new Track("Headless Cross").featuring(brianMay), 1, 2)
		);


		final Country germany = countryRepository.findByCode("DE").orElseGet(() -> countryRepository.save(new Country(("DE"))));

		final Band scorpions =
			this.artistRepository.save(new Band("Scorpions", germany));
		this.albumRepository.save(new Album(scorpions, "Fly To The Rainbow", Year.of(1974)));
		this.albumRepository.save(new Album(scorpions, "Crazy World", Year.of(1990)));

		final Band dieAerzte =
			this.artistRepository.save(new Band("Die Ärzte", germany));
		this.albumRepository.save(new Album(dieAerzte, "Die Bestie in Menschengestalt", Year.of(1993)));
		this.albumRepository.save(new Album(dieAerzte, "Planet Punk", Year.of(1995)));

		final SoloArtist farinUrlaub = this.artistRepository.save(new SoloArtist("Farin Urlaub"));
		final SoloArtist belaB = this.artistRepository.save(new SoloArtist("Bela B."));

		final Band rainbirds = this.artistRepository.save(new Band("Rainbirds", germany));

		final SoloArtist rod = this.artistRepository.save(new SoloArtist("Rodrigo González")
			.playedIn(rainbirds, Year.of(1988), Year.of(1989))
			.playedIn(dieAerzte, Year.of(1993), null));

		this.artistRepository.save(rod);

		final Band dth =
			this.artistRepository.save(new Band("Die Toten Hosen", germany));
		this.albumRepository.save(new Album(dth, "Kauf MICH!", Year.of(1993)));
		this.albumRepository.save(new Album(dth, "Opium fürs Volk", Year.of(1996)));


		this.session.query(String.class, "MATCH (album:Album) - [:RELEASED_BY] -> (artist:Artist) WHERE artist.name = $artist RETURN album.name",
			Map.of("artist", "Queen")).forEach(System.out::println);
		System.out.println("---");
		this.session.query(String.class, "MATCH (:Artist {name: $artist}) <- [:RELEASED_BY] - (a:Album) RETURN a.name",
			Map.of("artist", "Queen")).forEach(System.out::println);
	}
}
