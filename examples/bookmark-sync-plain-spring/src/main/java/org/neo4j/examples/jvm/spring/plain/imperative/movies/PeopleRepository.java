package org.neo4j.examples.jvm.spring.plain.imperative.movies;

import static org.neo4j.examples.jvm.spring.plain.imperative.movies.MovieRepository.asMovie;

import java.util.Map;
import java.util.Optional;

import org.neo4j.driver.Driver;
import org.neo4j.driver.types.Node;
import org.neo4j.examples.jvm.spring.plain.imperative.Neo4jBookmarkManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * @author Michael J. Simons
 */
@Component
final class PeopleRepository {

	private final Neo4jBookmarkManager bookmarkManager;

	PeopleRepository(Neo4jBookmarkManager bookmarkManager) {
		this.bookmarkManager = bookmarkManager;
	}

	Person save(Person person) {

		return bookmarkManager.doWithBookmarks(session -> {

			var query = """
				MERGE (p:Person {name: $name})
				SET p.born = $born
				RETURN p
				""";

			var personNode = session
				.writeTransaction(tx -> tx.run(
					query, Map.of("name", person.getName(), "born", person.getBorn())).single())
				.get("p").asNode();

			return asPerson(personNode);
		});
	}

	Optional<PersonDetails> getDetailsByName(String name) {
		return bookmarkManager.doWithBookmarks(session -> {

			var query = """
				MATCH (person:Person {name: $name})
				OPTIONAL MATCH (person)-[:DIRECTED]->(d:Movie)
				OPTIONAL MATCH (person)<-[r:ACTED_IN]->(a:Movie)
				OPTIONAL MATCH (person)-->(movies)<-[relatedRole:ACTED_IN]-(relatedPerson)		
				RETURN DISTINCT person,
				collect(DISTINCT d) AS directed,
				collect(DISTINCT a) AS actedIn,
				collect(DISTINCT relatedPerson) AS related
				""";

			return session.readTransaction(tx -> {
				var result = tx.run(query, Map.of("name", name));
				if (!result.hasNext()) {
					return Optional.empty();
				}

				var record = result.single();
				var person = asPerson(record.get("person").asNode());
				var directed = record.get("directed").asList(v -> asMovie(v.asNode()));
				var actedIn = record.get("actedIn").asList(v -> asMovie(v.asNode()));
				var related = record.get("related").asList(v -> asPerson(v.asNode()));

				return Optional.of(new PersonDetails(person.getName(), person.getBorn(), actedIn, directed, related));
			});
		});
	}

	static Person asPerson(Node personNode) {
		return new Person(personNode.id(), personNode.get("name").asString(), personNode.get("born").asInt());
	}
}
