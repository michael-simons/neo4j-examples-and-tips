
package io.helidon.examples.quickstart.mp;

import io.helidon.microprofile.server.Server;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.spi.CDI;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.testcontainers.containers.Neo4jContainer;

class MainTest {

	private static Neo4jContainer neo4jContainer;

	private static Server server;
	private static String serverUrl;

	@BeforeAll
	public static void startTheServer() throws Exception {
		server = Server.create().start();
		serverUrl = "http://localhost:" + server.port();

		neo4jContainer = new Neo4jContainer<>("neo4j:4.0")
			.withAdminPassword("secret");
		neo4jContainer.start();

		try (Driver driver = GraphDatabase.driver(neo4jContainer.getBoltUrl(), AuthTokens.basic("neo4j", "secret"));
			Session session = driver.session()) {
			session.writeTransaction(tx -> tx.run(
				"CREATE (TheMatrix:Movie {title:'The Matrix', released:1999, tagline:'Welcome to the Real World'})\n"
					+ "CREATE (Keanu:Person {name:'Keanu Reeves', born:1964})\n"
					+ "CREATE (Carrie:Person {name:'Carrie-Anne Moss', born:1967})\n"
					+ "CREATE (Laurence:Person {name:'Laurence Fishburne', born:1961})\n"
					+ "CREATE (Hugo:Person {name:'Hugo Weaving', born:1960})\n"
					+ "CREATE (LillyW:Person {name:'Lilly Wachowski', born:1967})\n"
					+ "CREATE (LanaW:Person {name:'Lana Wachowski', born:1965})\n"
					+ "CREATE (JoelS:Person {name:'Joel Silver', born:1952})\n"
					+ "CREATE\n"
					+ "  (Keanu)-[:ACTED_IN {roles:['Neo']}]->(TheMatrix),\n"
					+ "  (Carrie)-[:ACTED_IN {roles:['Trinity']}]->(TheMatrix),\n"
					+ "  (Laurence)-[:ACTED_IN {roles:['Morpheus']}]->(TheMatrix),\n"
					+ "  (Hugo)-[:ACTED_IN {roles:['Agent Smith']}]->(TheMatrix),\n"
					+ "  (LillyW)-[:DIRECTED]->(TheMatrix),\n"
					+ "  (LanaW)-[:DIRECTED]->(TheMatrix),\n"
					+ "  (JoelS)-[:PRODUCED]->(TheMatrix)").consume());
		}
	}

	@AfterAll
	public static void stopNeo4j() {
		neo4jContainer.stop();
	}

	@Test
	void testMovies() {
		Client client = ClientBuilder.newClient();

		var result = client
			.target(serverUrl)
			.path("api/movies")
			.request()
			.get(JsonArray.class);
		Assertions.assertEquals("The Matrix", result.getJsonObject(0).getString("title"));

	}

	@Test
	void testHelloWorld() {
		Client client = ClientBuilder.newClient();

		JsonObject jsonObject = client
			.target(serverUrl)
			.path("greet")
			.request()
			.get(JsonObject.class);
		Assertions.assertEquals("Hello World!", jsonObject.getString("message"),
			"default message");

		jsonObject = client
			.target(serverUrl)
			.path("greet/Joe")
			.request()
			.get(JsonObject.class);
		Assertions.assertEquals("Hello Joe!", jsonObject.getString("message"),
			"hello Joe message");

		Response r = client
			.target(serverUrl)
			.path("greet/greeting")
			.request()
			.put(Entity.entity("{\"greeting\" : \"Hola\"}", MediaType.APPLICATION_JSON));
		Assertions.assertEquals(204, r.getStatus(), "PUT status code");

		jsonObject = client
			.target(serverUrl)
			.path("greet/Jose")
			.request()
			.get(JsonObject.class);
		Assertions.assertEquals("Hola Jose!", jsonObject.getString("message"),
			"hola Jose message");

		r = client
			.target(serverUrl)
			.path("metrics")
			.request()
			.get();
		Assertions.assertEquals(200, r.getStatus(), "GET metrics status code");

		r = client
			.target(serverUrl)
			.path("health")
			.request()
			.get();
		Assertions.assertEquals(200, r.getStatus(), "GET health status code");
	}

	@AfterAll
	static void destroyClass() {
		CDI<Object> current = CDI.current();
		((SeContainer) current).close();
	}
}
