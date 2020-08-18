package org.neo4j.tips.cluster.sdn_ogm.api;

import java.security.Principal;
import java.util.Collection;

import org.neo4j.tips.cluster.sdn_ogm.domain.Movie;
import org.neo4j.tips.cluster.sdn_ogm.domain.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

	private final MovieService movieService;

	public MovieController(MovieService movieService) {
		this.movieService = movieService;
	}

	@GetMapping({ "", "/" })
	public Collection<Movie> getMovies() {
		return this.movieService.getAllMovies();
	}

	@PostMapping("/watched")
	public Integer watched(Principal principal, @RequestBody String title) {

		return this.movieService.watchMovie(principal.getName(), title);
	}
}
