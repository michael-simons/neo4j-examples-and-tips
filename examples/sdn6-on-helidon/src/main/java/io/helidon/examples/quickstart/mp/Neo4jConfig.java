package io.helidon.examples.quickstart.mp;

import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

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
	public Driver driver() {
		return GraphDatabase
			.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "secret"), Config.builder().withLogging(
				Logging.javaUtilLogging(Level.INFO)).build());
	}

	public void close(@Disposes Driver driver) {
		driver.close();
	}
}
