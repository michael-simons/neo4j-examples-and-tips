package ac.simons.neo4j.sdn_on_liberty.api;

import ac.simons.neo4j.sdn_on_liberty.domain.Movie;
import ac.simons.neo4j.sdn_on_liberty.domain.MovieRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("movies")
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
