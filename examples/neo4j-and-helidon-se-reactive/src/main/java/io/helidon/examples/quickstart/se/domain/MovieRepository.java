package io.helidon.examples.quickstart.se.domain;

import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.Single;

import java.util.concurrent.atomic.AtomicReference;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxSession;
import org.reactivestreams.FlowAdapters;

public final class MovieRepository {

	private final Driver driver;

	public MovieRepository(Driver driver) {
		this.driver = driver;
	}

	public Multi<Movie> findAll() {

		var sessionHolder = new AtomicReference<RxSession>();
		return Single
			.defer(() -> {
				var session = driver.rxSession();
				sessionHolder.set(session);
				return Single.just(session);
			})
			.flatMap(this::executeQuery)
			.onTerminate(() -> Single
				.create(FlowAdapters.toFlowPublisher(sessionHolder.get().close()))
				.toOptionalSingle()
				.subscribe(empty -> {}));
	}

	private Multi<Movie> executeQuery(RxSession rxSession) {
		var query = ""
			+ "match (m:Movie) "
			+ "match (m) <- [:DIRECTED] - (d:Person) "
			+ "match (m) <- [r:ACTED_IN] - (a:Person) "
			+ "return m, collect(d) as directors, collect({name:a.name, roles: r.roles}) as actors";

		return Multi
			.create(FlowAdapters.toFlowPublisher(rxSession.readTransaction(tx -> tx.run(query).records())))
			.map(r -> {
				var movieNode = r.get("m").asNode();

				var directors = r.get("directors").asList(v -> {
					var personNode = v.asNode();
					return new Person(personNode.get("born").asInt(), personNode.get("name").asString());
				});

				var actors = r.get("actors").asList(v -> new Actor(v.get("name").asString(), v.get("roles").asList(Value::asString)));

				var m = new Movie(movieNode.get("title").asString(), movieNode.get("tagline").asString());
				m.setReleased(movieNode.get("released").asInt());
				m.setDirectorss(directors);
				m.setActors(actors);
				return m;
			});
	}
}
