package org.neo4j.tips.sdn.sdn53withembeddedneo4joverbolt;

import org.neo4j.tips.sdn.sdn53withembeddedneo4joverbolt.domain.Movie;
import org.neo4j.tips.sdn.sdn53withembeddedneo4joverbolt.domain.MovieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ExampleUsage implements CommandLineRunner {

	private final MovieRepository movieRepository;

	public ExampleUsage(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@Override
	public void run(String... args) {

		var movie = movieRepository.save(new Movie("One flew over the coconest"));
		System.out.println("Saved a movie: " + movie);
		System.exit(0);
	}
}
