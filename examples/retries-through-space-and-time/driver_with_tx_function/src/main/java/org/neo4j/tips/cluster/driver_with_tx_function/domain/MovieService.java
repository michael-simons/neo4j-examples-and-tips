package org.neo4j.tips.cluster.driver_with_tx_function.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.tips.cluster.driver_with_tx_function.support.InsertRandom;
import org.springframework.core.log.LogMessage;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

	private static final Log log = LogFactory.getLog(MovieService.class);

	private final Driver driver;

	MovieService(Driver driver) {
		this.driver = driver;
	}

	public Collection<Movie> getAllMovies() {

		TransactionWork<List<Movie>> readAllMovies = tx -> {
			Function<Record, Movie> recordToMovie = r -> new Movie(r.get("m").get("title").asString());
			return tx.run("MATCH (m:Movie) RETURN m ORDER BY m.title ASC").list(recordToMovie);
		};

		try (Session session = driver.session()) {
			return session.readTransaction(readAllMovies);
		}
	}

	public Integer watchMovie(String userName, String title) {

		TransactionWork<Integer> watchMovie = tx -> {

			log.info(LogMessage.format("Watching movie '%s' as %s", title, userName));

			var userId = tx.run("MERGE (u:Person {name: $name}) RETURN id(u)", Map.of("name", userName))
				.single().get(0).asLong();

			var movieId = tx.run("MERGE (m:Movie {title: $title}) RETURN id(m)", Map.of("title", title))
				.single().get(0).asLong();

			InsertRandom.delay();

			return tx.run(""
				+ "MATCH (m:Movie), (u:Person)\n"
				+ "WHERE id(m) = $movieId AND id(u) = $userId WITH m, u\n"
				+ "MERGE (u) - [w:WATCHED] -> (m)\n"
				+ "SET w.number_of_times = COALESCE(w.number_of_times,0)+1\n"
				+ "RETURN w.number_of_times AS number_of_times", Map.of("movieId", movieId, "userId", userId))
				.single().get("number_of_times").asInt();
		};

		try (Session session = driver.session()) {
			return session.writeTransaction(watchMovie);
		}
	}
}
