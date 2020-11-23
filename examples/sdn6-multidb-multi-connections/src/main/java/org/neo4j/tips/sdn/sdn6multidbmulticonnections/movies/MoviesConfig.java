package org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies;

import java.util.Set;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.core.DatabaseSelection;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.convert.Neo4jConversions;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.PlatformTransactionManager;

@EnableNeo4jRepositories(
	basePackageClasses = MoviesConfig.class,
	neo4jMappingContextRef = "moviesContext",
	neo4jTemplateRef = "moviesTemplate",
	transactionManagerRef = "moviesManager"
)
@Configuration(proxyBeanMethods = false)
public class MoviesConfig {

	@Primary
	@Bean("moviesDriver")
	public Driver driver(Neo4jProperties neo4jProperties) {

		var authentication = neo4jProperties.getAuthentication();
		return GraphDatabase.driver(neo4jProperties.getUri(), AuthTokens.basic(
			authentication.getUsername(), authentication
				.getPassword()));
	}

	@Primary @Bean("moviesClient")
	public Neo4jClient neo4jClient(Driver driver) {
		return Neo4jClient.create(driver);
	}

	@Primary @Bean(name = "moviesTemplate")
	public Neo4jOperations neo4jTemplate(
		@Qualifier("moviesClient") Neo4jClient neo4jClient,
		@Qualifier("moviesContext") Neo4jMappingContext mappingContext
	) {
		return new Neo4jTemplate(neo4jClient, mappingContext, () -> DatabaseSelection.byName("movies"));
	}

	@Primary @Bean("moviesManager")
	public PlatformTransactionManager transactionManager(@Qualifier("moviesDriver") Driver driver) {
		return new Neo4jTransactionManager(driver, () -> DatabaseSelection.byName("movies"));
	}

	@Primary @Bean("moviesContext")
	public Neo4jMappingContext neo4jMappingContext(ApplicationContext applicationContext,
		Neo4jConversions neo4jConversions) throws ClassNotFoundException {
		Set<Class<?>> initialEntityClasses = (new EntityScanner(applicationContext)).scan(new Class[] { Node.class });
		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		context.setInitialEntitySet(initialEntityClasses);
		return context;
	}
}
