package com.example.bookmarksyncsdn5.movies;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons
 */
@RestController
@RequestMapping("/api/movies")
public final class MoviesController {

	private final MovieRepository movieRepository;

	MoviesController(MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@GetMapping({ "", "/" })
	public List<Movie> get() {
		return StreamSupport.stream(movieRepository.findAll(Sort.by("title").ascending()).spliterator(), false).collect(
			Collectors.toList());
	}
}
