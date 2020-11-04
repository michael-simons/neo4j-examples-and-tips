package io.helidon.examples.quickstart.se.api;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.bind.JsonbBuilder;

import io.helidon.examples.quickstart.se.domain.MovieRepository;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

public class MovieService implements Service {

	private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Map.of());

	private final MovieRepository movieRepository;

	public MovieService(final MovieRepository movieRepository) {
		this.movieRepository = movieRepository;
	}

	@Override
    public void update(Routing.Rules rules) {
        rules.get("/api/movies", this::findMoviesHandler);
	}
	
	private void findMoviesHandler(ServerRequest request, ServerResponse response) {
		var movies = this.movieRepository.findAll();
		
		var json = JsonbBuilder.create().toJson(movies);
		response.send(json);
    }
}
