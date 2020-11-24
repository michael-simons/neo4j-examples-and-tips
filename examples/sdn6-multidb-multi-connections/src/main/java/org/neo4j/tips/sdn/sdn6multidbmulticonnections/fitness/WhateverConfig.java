package org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness;

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
	basePackageClasses = WhateverConfig.class,
	neo4jMappingContextRef = "fitnessContext",
	neo4jTemplateRef = "fitnessTemplate",
	transactionManagerRef = "fitnessManager"
)
public class WhateverConfig {

	@Bean
	public Driver fitnessDriver(@Qualifier("fitnessProperties") Neo4jProperties neo4jProperties) {

		var authentication = neo4jProperties.getAuthentication();
		return GraphDatabase.driver(neo4jProperties.getUri(), AuthTokens.basic(
			authentication.getUsername(), authentication
				.getPassword()));
	}

	@Bean
	public Neo4jClient fitnessClient(@Qualifier("fitnessDriver") Driver driver) {
		return Neo4jClient.create(driver);
	}

	@Bean
	public Neo4jOperations fitnessTemplate(
		@Qualifier("fitnessClient") Neo4jClient fitnessClient,
		@Qualifier("fitnessContext") Neo4jMappingContext fitnessContext,
		@Qualifier("fitnessSelection") DatabaseSelectionProvider fitnessSelection
	) {
		return new Neo4jTemplate(fitnessClient, fitnessContext, fitnessSelection);
	}

	@Bean
	public PlatformTransactionManager fitnessManager(
		@Qualifier("fitnessDriver") Driver driver,
		@Qualifier("fitnessSelection") DatabaseSelectionProvider fitnessSelection
	) {
		return new Neo4jTransactionManager(driver, fitnessSelection);
	}

	@Bean
	public DatabaseSelectionProvider fitnessSelection(
		@Qualifier("fitnessDataProperties") Neo4jDataProperties dataProperties) {
		return () -> DatabaseSelection.byName(dataProperties.getDatabase());
	}

	@Bean
	public Neo4jMappingContext fitnessContext(Neo4jConversions neo4jConversions) throws ClassNotFoundException {

		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		// See https://jira.spring.io/browse/DATAGRAPH-1441
		// context.setStrict(true);
		context.setInitialEntitySet(Neo4jPropertiesConfig.scanForEntities(this.getClass().getPackageName()));
		return context;
	}
}
