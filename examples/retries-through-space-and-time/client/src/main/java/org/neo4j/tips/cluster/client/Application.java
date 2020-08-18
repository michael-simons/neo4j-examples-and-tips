package org.neo4j.tips.cluster.client;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Application {

	public static void main(String... args) throws Exception {

		var httpClient = HttpClient.newBuilder().authenticator(new Authenticator() {
			@Override protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("couchpotato", "secret".toCharArray());
			}
		}).build();

		var objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		var watcher = new Watcher(httpClient, objectMapper);
		var executor = Executors.newCachedThreadPool();

		System.out.println("Starting to watch random movies until interrupted.");

		while (true) {
			var couchPotatoes = executor
				.invokeAll(List.of(() -> watcher.watchRandomMovie(), () -> watcher.watchRandomMovie()));
			couchPotatoes.forEach(message -> {
				try {
					System.out.println(message.get());
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					System.err.println("Error watching a random movie " + e.getMessage());
				}
			});
			Thread.sleep(5_000);
		}
	}
}
