package org.neo4j.examples;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.reactive.RxSession;

@Path("/artists")
public class ArtistsResource {

	private final Driver driver;

	@Inject
	public ArtistsResource(Driver driver) {
		this.driver = driver;
	}

	private Multi<Record> executeArtistQuery(RxSession session) {
		return Multi.createFrom().publisher(
			session.readTransaction(tx -> tx.run("MATCH (n:Artist) RETURN n.name AS name").records())
		);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Multi<String> getArtists() {
		return Uni.createFrom().item(driver::rxSession)
			.toMulti()
			.flatMap(this::executeArtistQuery)
			.map(record -> record.get("name").asString());
	}
}

/*
		@Produces(MediaType.SERVER_SENT_EVENTS)
		@SseElementType(MediaType.TEXT_PLAIN)
	 */