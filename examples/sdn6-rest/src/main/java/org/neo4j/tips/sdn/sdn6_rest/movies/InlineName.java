package org.neo4j.tips.sdn.sdn6_rest.movies;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "inlineName", types = { Person.class })
public interface InlineName {

	String getName();
}