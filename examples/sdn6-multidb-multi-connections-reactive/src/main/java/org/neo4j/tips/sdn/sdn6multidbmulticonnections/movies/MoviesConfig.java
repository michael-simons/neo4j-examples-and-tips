package org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies;

import reactor.core.publisher.Mono;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.health.DatabaseSelectionAwareNeo4jReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.neo4j.config.Neo4jEntityScanner;
import org.springframework.data.neo4j.core.DatabaseSelection;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.data.neo4j.core.ReactiveNeo4jClient;
import org.springframework.data.neo4j.core.ReactiveNeo4jOperations;
import org.springframework.data.neo4j.core.ReactiveNeo4jTemplate;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration(proxyBeanMethods = false)
@EnableReactiveNeo4jRepositories(
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
	public ReactiveNeo4jClient moviesClient(Driver driver, ReactiveDatabaseSelectionProvider moviesSelection) {
		return ReactiveNeo4jClient.create(driver, moviesSelection);
	}

	@Primary @Bean
	public DatabaseSelectionAwareNeo4jReactiveHealthIndicator movieHealthIndicator(Driver driver,
		ReactiveDatabaseSelectionProvider moviesSelection) {
		return new DatabaseSelectionAwareNeo4jReactiveHealthIndicator(driver, moviesSelection);
	}

	@Primary @Bean
	public ReactiveNeo4jOperations moviesTemplate(ReactiveNeo4jClient moviesClient, Neo4jMappingContext moviesContext) {
		return new ReactiveNeo4jTemplate(moviesClient, moviesContext);
	}

	@Primary @Bean
	public ReactiveTransactionManager moviesManager(Driver driver, ReactiveDatabaseSelectionProvider moviesSelection
	) {
		return new ReactiveNeo4jTransactionManager(driver, moviesSelection);
	}

	@Primary @Bean
	public ReactiveDatabaseSelectionProvider moviesSelection(Neo4jDataProperties dataProperties) {
		return () -> Mono.just(DatabaseSelection.byName(dataProperties.getDatabase()));
	}

	@Primary @Bean
	public Neo4jMappingContext moviesContext(ResourceLoader resourceLoader, Neo4jConversions neo4jConversions)
		throws ClassNotFoundException {

		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		context.setInitialEntitySet(Neo4jEntityScanner.get(resourceLoader).scan(this.getClass().getPackageName()));
		return context;
	}
}
