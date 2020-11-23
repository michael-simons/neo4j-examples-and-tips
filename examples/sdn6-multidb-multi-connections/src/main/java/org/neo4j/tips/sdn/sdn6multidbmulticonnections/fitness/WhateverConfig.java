package org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness;

import java.util.Set;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.neo4j.config.AbstractNeo4jConfig;
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

@Configuration
@EnableNeo4jRepositories(
	basePackageClasses = WhateverConfig.class,
	neo4jMappingContextRef = "fitnessContext",
	neo4jTemplateRef = "fitnessTemplate",
	transactionManagerRef = "fitnessManager"
)
public class WhateverConfig  {

	@Bean("fitnessDriver")
	public Driver driver(@Qualifier("fitnessProperties") Neo4jProperties neo4jProperties) {

		var authentication = neo4jProperties.getAuthentication();
		return GraphDatabase.driver(neo4jProperties.getUri(), AuthTokens.basic(
			authentication.getUsername(), authentication
				.getPassword()));
	}

	@Bean("fitnessClient")
	public Neo4jClient neo4jClient(@Qualifier("fitnessDriver") Driver driver) {
		return Neo4jClient.create(driver);
	}

	@Bean(name = "fitnessTemplate")
	public Neo4jOperations neo4jTemplate(
		@Qualifier("fitnessClient") Neo4jClient neo4jClient,
		@Qualifier("fitnessContext") Neo4jMappingContext mappingContext
	) {
		return new Neo4jTemplate(neo4jClient, mappingContext, () -> DatabaseSelection.byName("fitness"));
	}

	@Bean("fitnessManager")
	public PlatformTransactionManager transactionManager(@Qualifier("fitnessDriver") Driver driver) {
		return new Neo4jTransactionManager(driver, () -> DatabaseSelection.byName("fitness"));
	}

	@Bean("fitnessContext")
	public Neo4jMappingContext neo4jMappingContext(ApplicationContext applicationContext,
		Neo4jConversions neo4jConversions) throws ClassNotFoundException {
		Set<Class<?>> initialEntityClasses = (new EntityScanner(applicationContext)).scan(new Class[] { Node.class });
		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		context.setInitialEntitySet(initialEntityClasses);
		return context;
	}
}
