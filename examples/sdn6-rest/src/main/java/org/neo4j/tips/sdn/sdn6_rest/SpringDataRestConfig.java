package org.neo4j.tips.sdn.sdn6_rest;

import org.neo4j.tips.sdn.sdn6_rest.movies.Movie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration(proxyBeanMethods = false)
public class SpringDataRestConfig {

	@Bean
	RepositoryRestConfigurer repositoryRestConfigurer() {
		return new RepositoryRestConfigurer() {
			@Override
			public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
				config.exposeIdsFor(Movie.class);
			}

		};
	}
}
