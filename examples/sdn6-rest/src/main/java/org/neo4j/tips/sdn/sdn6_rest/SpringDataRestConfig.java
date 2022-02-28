package org.neo4j.tips.sdn.sdn6_rest;

import org.neo4j.tips.sdn.sdn6_rest.movies.Actor;
import org.neo4j.tips.sdn.sdn6_rest.movies.Movie;
import org.neo4j.tips.sdn.sdn6_rest.movies.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
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

	@Bean
	RepresentationModelProcessor<EntityModel<Actor>> actorResourceProcessor(final EntityLinks entityLinks) {
		// Don't use a lambda here, otherwise the type will evaporate (some Spring thing I really don't wanna debug right now)
		return new RepresentationModelProcessor<EntityModel<Actor>>() {
			@Override public EntityModel<Actor> process(EntityModel<Actor> model) {
				if (model.getContent().getPerson() != null) {
					model.add(entityLinks.linkToItemResource(Person.class, model.getContent().getPerson().getId()));
				}
				return model;
			}
		};
	}
}
