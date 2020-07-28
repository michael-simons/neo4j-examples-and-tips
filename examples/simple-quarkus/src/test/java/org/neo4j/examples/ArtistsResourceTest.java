package org.neo4j.examples;

import static io.restassured.RestAssured.*;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

@QuarkusTest
public class ArtistsResourceTest {

	@Test
	public void testArtistsEndpoint() {
		given()
			.when().get("/artists")
			.then()
			.statusCode(200);
	}
}