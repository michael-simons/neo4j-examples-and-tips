/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.examples.jvm.spring.plain.imperative.movies;

import static org.neo4j.examples.jvm.spring.plain.imperative.movies.PeopleRepository.asPerson;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Value;
import org.neo4j.examples.jvm.spring.plain.imperative.Neo4jBookmarkManager;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * @author Michael J. Simons
 */
@Component
final class MovieRepository {

	private final Neo4jBookmarkManager bookmarkManager;

	MovieRepository(Neo4jBookmarkManager bookmarkManager) {
		this.bookmarkManager = bookmarkManager;
	}

	List<Movie> findAll() {

		return bookmarkManager.doWithBookmarks(session -> {

			var query = """
				MATCH (m:Movie)
				MATCH (m) <- [:DIRECTED] - (d:Person)
				MATCH (m) <- [r:ACTED_IN] - (a:Person)
				RETURN m, collect(DISTINCT d) AS directors, collect(DISTINCT {name:a.name, roles: r.roles}) AS actors
				ORDER BY m.name ASC
				""";

			return session.readTransaction(tx -> tx.run(query).list(r -> {
				var movieNode = r.get("m").asNode();

				var directors = r.get("directors").asList(v -> asPerson(v.asNode()));
				var actors = r.get("actors").asList(v -> new Actor(v.get("name").asString(), v.get("roles").asList(Value::asString)));

				Movie m = asMovie(movieNode);
				m.addDirectors(directors);
				m.addActors(actors);

				return m;
			}));
		});
	}

	static Movie asMovie(org.neo4j.driver.types.Node movieNode) {
		var m = new Movie(movieNode.get("title").asString(), movieNode.get("tagline").asString());
		m.setReleased(movieNode.get("released").asInt());
		return m;
	}
}
