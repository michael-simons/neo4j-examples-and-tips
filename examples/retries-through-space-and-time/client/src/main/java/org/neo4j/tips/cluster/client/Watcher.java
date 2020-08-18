package org.neo4j.tips.cluster.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Watcher {

	private final HttpClient httpClient;

	private final ObjectMapper objectMapper;

	public Watcher(HttpClient httpClient, ObjectMapper objectMapper) {
		this.httpClient = httpClient;
		this.objectMapper = objectMapper;
	}

	private Movie getRandomMovie() throws IOException, InterruptedException {

		var response = httpClient.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/movies"))
				.GET().build(),
			BodyHandlers.ofInputStream()
		);

		if (response.statusCode() != 200) {
			throw new RuntimeException("Could get all movies: " + response.statusCode());
		}

		var movies = objectMapper.readValue(response.body(), Movie[].class);
		return movies[ThreadLocalRandom.current().nextInt(movies.length)];
	}

	public String watchRandomMovie() throws IOException, InterruptedException {

		var selectedMovie = getRandomMovie();
		var requestBody = HttpRequest.BodyPublishers.ofString(selectedMovie.getTitle());

		var watchedMovieResponse = httpClient.send(
			HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/movies/watched"))
				.POST(requestBody).build(),
			BodyHandlers.ofString()
		);

		if (watchedMovieResponse.statusCode() != 200) {
			throw new RuntimeException("Could not watch movie: " + watchedMovieResponse.statusCode());
		}

		return "Watched " + selectedMovie.getTitle() + " " + watchedMovieResponse.body() + " times.";
	}
}
