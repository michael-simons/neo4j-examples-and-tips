package ac.simons.neo4j.sdn_on_liberty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;

public class Neo4jConfig {

	@Produces
	@ApplicationScoped
	public Driver driver() {
		return GraphDatabase
			.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "secret"), Config.builder().withLogging(
				Logging.slf4j()).build());
	}

	public void close(@Disposes Driver driver) {
		driver.close();
	}
}
