package org.neo4j.tips.cluster.sdn_ogm.domain;

import io.github.resilience4j.retry.annotation.Retry;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.tips.cluster.sdn_ogm.support.InsertRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.core.log.LogMessage;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("!use-sdn")
@Service
@Retry(name = "neo4j")
public class MovieServiceBasedOnTemplate implements MovieService {

	private static final Log log = LogFactory.getLog(MovieServiceBasedOnTemplate.class);

	private final Neo4jTemplate neo4jTemplate;

	public MovieServiceBasedOnTemplate(Neo4jTemplate neo4jTemplate) {
		this.neo4jTemplate = neo4jTemplate;
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Movie> getAllMovies() {

		return neo4jTemplate.findAll(Movie.class);
	}

	@Override
	@Transactional
	public Integer watchMovie(String userName, String title) {

		log.info(LogMessage.format("Watching movie '%s' as %s", title, userName));

		var user = neo4jTemplate.findOne(
			"MATCH (u:Person) -[w:WATCHED] -> (m:Movie) WHERE u.name = $name RETURN u, collect(w), collect(m)",
			Map.of("name", userName), User.class)
			.orElseGet(() -> new User(userName));

		var movie = neo4jTemplate
			.findOne("MATCH (m:Movie) WHERE m.title = $title RETURN m", Map.of("title", title), Movie.class)
			.orElseGet(() -> new Movie(title));

		InsertRandom.delay();

		int numberOfTimes = user.watch(movie);
		neo4jTemplate.save(user);
		return numberOfTimes;
	}
}
