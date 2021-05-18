package org.neo4j.tips.sdn.sdn6multidbmulticonnections.health;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.HealthContributorNameFactory;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Neo4jHealthConfig {

	@Bean
	ReactiveHealthContributor neo4jHealthIndicator(
		Map<String, DatabaseSelectionAwareNeo4jReactiveHealthIndicator> customNeo4jHealthIndicators) {
		return CompositeReactiveHealthContributor.fromMap(customNeo4jHealthIndicators);
	}

	@Bean
	InitializingBean healthContributorRegistryCleaner(HealthContributorRegistry healthContributorRegistry,
		Map<String, DatabaseSelectionAwareNeo4jReactiveHealthIndicator> customNeo4jHealthIndicators) {
		return () -> customNeo4jHealthIndicators.keySet()
			.stream()
			.map(HealthContributorNameFactory.INSTANCE)
			.forEach(healthContributorRegistry::unregisterContributor);
	}

	@Bean
	InitializingBean reactiveHealthContributorRegistryCleaner(
		ReactiveHealthContributorRegistry healthContributorRegistry,
		Map<String, DatabaseSelectionAwareNeo4jReactiveHealthIndicator> customNeo4jHealthIndicators) {
		return () -> customNeo4jHealthIndicators.keySet()
			.stream()
			.map(HealthContributorNameFactory.INSTANCE)
			.forEach(healthContributorRegistry::unregisterContributor);
	}
}
