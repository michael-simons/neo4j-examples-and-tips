package org.neo4j.tips.sdn.sdn6;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface MovieRepository extends ReactiveNeo4jRepository<Movie, Long> {

	Flux<Movie> findAllByTitleMatches(String name);

	@Query("WITH apoc.create.vNode(['Movie'],{title:'Matrix 4.1'}) as m\n"
		+ "MATCH (e:Person {name: 'Emil Eifrem'})\n"
		+ "WITH m, e, apoc.create.vRelationship(e,'ACTED_IN',{roles:['himself']},m) AS r "
		+ "RETURN m, collect(r), collect(e)")
	Mono<Movie> findVirtualMovieViaApoc();
}
