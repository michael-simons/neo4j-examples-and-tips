package org.neo4j.tips.cluster.sdn_ogm.domain;

import io.github.resilience4j.retry.annotation.Retry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.ogm.session.Session;
import org.neo4j.tips.cluster.sdn_ogm.support.InsertRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.core.log.LogMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Retry(name = "neo4j")
@Profile("!use-sdn")
public class MovieServiceBasedOnPureOGM implements MovieService {

	private static final Log log = LogFactory.getLog(MovieServiceBasedOnPureOGM.class);

	private final Session session;

	public MovieServiceBasedOnPureOGM(Session session) {
		this.session = session;
	}

	@Transactional(readOnly = true)
	public Collection<Movie> getAllMovies() {

		return session.loadAll(Movie.class);
	}

	@Transactional
	public Integer watchMovie(String userName, String title) {

		log.info(LogMessage.format("Watching movie '%s' as %s", title, userName));

		var user = Optional.ofNullable(
			session.queryForObject(User.class,
				"MATCH (u:Person) -[w:WATCHED] -> (m:Movie) WHERE u.name = $name RETURN u, w, m",
				Map.of("name", userName)))
			.orElseGet(() -> new User(userName));

		var movie = Optional.ofNullable(
			session
				.queryForObject(Movie.class, "MATCH (m:Movie) WHERE m.title = $title RETURN m", Map.of("title", title)))
			.orElseGet(() -> new Movie(title));

		InsertRandom.delay();

		int numberOfTimes = user.watch(movie);
		session.save(user);
		return numberOfTimes;
	}
}
