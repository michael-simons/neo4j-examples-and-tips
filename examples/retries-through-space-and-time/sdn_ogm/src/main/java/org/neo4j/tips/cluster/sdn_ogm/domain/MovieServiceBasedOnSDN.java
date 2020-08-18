package org.neo4j.tips.cluster.sdn_ogm.domain;

import io.github.resilience4j.retry.annotation.Retry;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.tips.cluster.sdn_ogm.support.InsertRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.core.log.LogMessage;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("use-sdn")
@Service
@Retry(name = "neo4j")
@EnableNeo4jRepositories(considerNestedRepositories = true)
public class MovieServiceBasedOnSDN implements MovieService {

	interface MovieRepository extends Neo4jRepository<Movie, Long> {

		Optional<Movie> findOneByTitle(String title);
	}

	interface UserRepository extends Neo4jRepository<User, Long> {

		Optional<User> findOneByName(String name);
	}

	private static final Log log = LogFactory.getLog(MovieServiceBasedOnSDN.class);

	private final MovieRepository movieRepository;

	private final UserRepository userRepository;

	public MovieServiceBasedOnSDN(MovieRepository movieRepository, UserRepository userRepository) {
		this.movieRepository = movieRepository;
		this.userRepository = userRepository;
	}

	@Override @Transactional(readOnly = true)
	public Collection<Movie> getAllMovies() {

		return (Collection<Movie>) movieRepository.findAll();
	}

	@Override @Transactional
	public Integer watchMovie(String userName, String title) {

		log.info(LogMessage.format("Watching movie '%s' as %s", title, userName));

		var user = userRepository.findOneByName(userName)
			.orElseGet(() -> new User(userName));

		var movie = movieRepository.findOneByTitle(title)
			.orElseGet(() -> new Movie(title));

		InsertRandom.delay();

		int numberOfTimes = user.watch(movie);
		userRepository.save(user);
		return numberOfTimes;
	}
}
