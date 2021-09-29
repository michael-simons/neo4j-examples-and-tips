package org.neo4j.tips.sdn.sdn6multidbmulticonnections;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

@Configuration(proxyBeanMethods = false)
public class Neo4jPropertiesConfig {

	@Bean
	@Primary
	@ConfigurationProperties("spring.neo4j")
	public Neo4jProperties moviesProperties() {
		return new Neo4jProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("spring.data.neo4j")
	public Neo4jDataProperties moviesDataProperties() {
		return new Neo4jDataProperties();
	}

	@Bean
	@ConfigurationProperties("fitness.spring.neo4j")
	public Neo4jProperties fitnessProperties() {
		return new Neo4jProperties();
	}

	@Bean
	@ConfigurationProperties("fitness.spring.data.neo4j")
	public Neo4jDataProperties fitnessDataProperties() {
		return new Neo4jDataProperties();
	}
}
