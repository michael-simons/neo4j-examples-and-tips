package org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness;

import reactor.core.publisher.Mono;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.health.DatabaseSelectionAwareNeo4jReactiveHealthIndicator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	public ReactiveNeo4jClient fitnessClient(@Qualifier("fitnessDriver") Driver driver,
		@Qualifier("fitnessSelection") ReactiveDatabaseSelectionProvider fitnessSelection) {
		return ReactiveNeo4jClient.create(driver, fitnessSelection);
	}

	@Bean
	public DatabaseSelectionAwareNeo4jReactiveHealthIndicator fitnessHealthIndicator(
		@Qualifier("fitnessDriver") Driver driver,
		@Qualifier("fitnessSelection") ReactiveDatabaseSelectionProvider moviesSelection) {
		return new DatabaseSelectionAwareNeo4jReactiveHealthIndicator(driver, moviesSelection);
	}

	@Bean
	public ReactiveNeo4jOperations fitnessTemplate(
		@Qualifier("fitnessClient") ReactiveNeo4jClient fitnessClient,
		@Qualifier("fitnessContext") Neo4jMappingContext fitnessContext
	) {
		return new ReactiveNeo4jTemplate(fitnessClient, fitnessContext);
	}

	@Bean
	public ReactiveTransactionManager fitnessManager(
		@Qualifier("fitnessDriver") Driver driver,
		@Qualifier("fitnessSelection") ReactiveDatabaseSelectionProvider fitnessSelection
	) {
		return new ReactiveNeo4jTransactionManager(driver, fitnessSelection);
	}

	@Bean
	public ReactiveDatabaseSelectionProvider fitnessSelection(
		@Qualifier("fitnessDataProperties") Neo4jDataProperties dataProperties) {
		return () -> Mono.just(DatabaseSelection.byName(dataProperties.getDatabase()));
	}

	@Bean
	public Neo4jMappingContext fitnessContext(ResourceLoader resourceLoader, Neo4jConversions neo4jConversions)
		throws ClassNotFoundException {

		Neo4jMappingContext context = new Neo4jMappingContext(neo4jConversions);
		context.setInitialEntitySet(Neo4jEntityScanner.get(resourceLoader).scan(this.getClass().getPackageName()));
		return context;
	}
}
