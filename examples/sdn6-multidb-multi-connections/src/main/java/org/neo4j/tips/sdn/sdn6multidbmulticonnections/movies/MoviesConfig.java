package org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies;

import java.util.Set;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.Neo4jPropertiesConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataProperties;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelection;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.schema.Node;
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
	public Driver moviesDriver(@Qualifier("moviesProperties") Neo4jProperties neo4jProperties) {

		var authentication = neo4jProperties.getAuthentication();
		return GraphDatabase.driver(neo4jProperties.getUri(), AuthTokens.basic(
			authentication.getUsername(), authentication
				.getPassword()));
	}

	@Primary @Bean
	public Neo4jClient moviesClient(@Qualifier("moviesDriver") Driver driver) {
		return Neo4jClient.create(driver);
	}

	@Primary @Bean
	public Neo4jOperations moviesTemplate(
		@Qualifier("moviesClient") Neo4jClient moviesClient,
		@Qualifier("moviesContext") Neo4jMappingContext moviesContext,
		@Qualifier("moviesSelection") DatabaseSelectionProvider moviesSelection
	) {
		return new Neo4jTemplate(moviesClient, moviesContext, moviesSelection);
	}

	@Primary @Bean
	public PlatformTransactionManager moviesManager(
		@Qualifier("moviesDriver") Driver driver,
		@Qualifier("moviesSelection") DatabaseSelectionProvider moviesSelection
	) {
		return new Neo4jTransactionManager(driver, moviesSelection);
	}

	@Primary @Bean
	public DatabaseSelectionProvider moviesSelection(
		@Qualifier("moviesDataProperties") Neo4jDataProperties dataProperties) {
		return () -> DatabaseSelection.byName(dataProperties.getDatabase());
	}

	@Primary @Bean
	public Neo4jMappingContext moviesContext(Neo4jConversions neo4jConversions) throws ClassNotFoundException {

		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		// See https://jira.spring.io/browse/DATAGRAPH-1441
		// context.setStrict(true);
		context.setInitialEntitySet(Neo4jPropertiesConfig.scanForEntities(this.getClass().getPackageName()));
		return context;
	}
}
