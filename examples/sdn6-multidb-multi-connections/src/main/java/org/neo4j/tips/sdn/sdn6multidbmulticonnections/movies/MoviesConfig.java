package org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.health.DatabaseSelectionAwareNeo4jHealthIndicator;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.neo4j.config.Neo4jEntityScanner;
import org.springframework.data.neo4j.core.DatabaseSelection;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableNeo4jRepositories(
	basePackageClasses = MoviesConfig.class,
	neo4jMappingContextRef = "moviesContext",
	neo4jTemplateRef = "moviesTemplate",
	transactionManagerRef = "moviesManager"
)
public class MoviesConfig {

	@Primary @Bean
	public Driver moviesDriver(Neo4jProperties neo4jProperties) {

		var authentication = neo4jProperties.getAuthentication();
		return GraphDatabase.driver(neo4jProperties.getUri(), AuthTokens.basic(
			authentication.getUsername(), authentication
				.getPassword()));
	}

	@Primary @Bean
	public Neo4jClient moviesClient(Driver driver, DatabaseSelectionProvider moviesSelection) {
		return Neo4jClient.create(driver, moviesSelection);
	}

	@Primary @Bean
	public Neo4jOperations moviesTemplate(
		Neo4jClient moviesClient,
		Neo4jMappingContext moviesContext
	) {
		return new Neo4jTemplate(moviesClient, moviesContext);
	}

	@Primary @Bean
	public DatabaseSelectionAwareNeo4jHealthIndicator movieHealthIndicator(Driver driver,
		DatabaseSelectionProvider moviesSelection) {
		return new DatabaseSelectionAwareNeo4jHealthIndicator(driver, moviesSelection);
	}

	@Primary @Bean
	public PlatformTransactionManager moviesManager(Driver driver, DatabaseSelectionProvider moviesSelection
	) {
		return new Neo4jTransactionManager(driver, moviesSelection);
	}

	@Primary @Bean
	public DatabaseSelectionProvider moviesSelection(
		Neo4jDataProperties dataProperties) {
		return () -> DatabaseSelection.byName(dataProperties.getDatabase());
	}

	@Primary @Bean
	public Neo4jMappingContext moviesContext(ResourceLoader resourceLoader, Neo4jConversions neo4jConversions)
		throws ClassNotFoundException {

		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		context.setInitialEntitySet(Neo4jEntityScanner.get(resourceLoader).scan(this.getClass().getPackageName()));
		return context;
	}
}
