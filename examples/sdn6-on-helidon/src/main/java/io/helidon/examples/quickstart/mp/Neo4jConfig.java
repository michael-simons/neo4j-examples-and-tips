package io.helidon.examples.quickstart.mp;

import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;

/**
 * This is what I expect helidon to do for me based on properties.
 */
public class Neo4jConfig {

	@Produces
	@ApplicationScoped
	public Driver driver(
		@ConfigProperty(name = "neo4j.driver.uri") String url,
		@ConfigProperty(name = "neo4j.driver.authentication.username") String username,
		@ConfigProperty(name = "neo4j.driver.authentication.password") String password
	) {
		return GraphDatabase
			.driver(url, AuthTokens.basic(username, password), Config.builder().withLogging(
				Logging.javaUtilLogging(Level.INFO)).build());
	}

	public void close(@Disposes Driver driver) {
		driver.close();
	}
}
