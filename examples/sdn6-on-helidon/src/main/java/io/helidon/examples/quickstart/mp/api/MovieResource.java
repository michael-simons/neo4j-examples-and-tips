package io.helidon.examples.quickstart.mp.api;

import io.helidon.examples.quickstart.mp.domain.Movie;
import io.helidon.examples.quickstart.mp.domain.MovieRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/api/movies")
public class MovieResource {

	@Inject
	MovieRepository movieRepository;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Iterable<Movie> getMovies() {

		return movieRepository.findAll();
	}
}
