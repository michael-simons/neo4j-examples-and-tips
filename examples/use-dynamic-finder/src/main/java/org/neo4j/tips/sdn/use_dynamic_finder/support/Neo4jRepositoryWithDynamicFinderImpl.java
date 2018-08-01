/*
 * Copyright (c) 2018 "Neo4j, Inc." <https://neo4j.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.tips.sdn.use_dynamic_finder.support;

import java.io.Serializable;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.repository.support.SimpleNeo4jRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author Michael J. Simons
 */
// tag::custom-base-class-for-repositories[]
@NoRepositoryBean // <1>
public class Neo4jRepositoryWithDynamicFinderImpl<T, ID extends Serializable>
	extends SimpleNeo4jRepository<T, ID> { // <2>

	private final Class<T> domainClass; // <3>
	private final Session session;

	public Neo4jRepositoryWithDynamicFinderImpl(
		Class<T> domainClass, Session session
	) {
		super(domainClass, session); // <4>
		this.domainClass = domainClass;
		this.session = session;
	}

	public Iterable<T> findAllByPropertyValue( // <5>
		String property, Object value
	) {
		return this.session.loadAll(
			this.domainClass,
			new Filter(property, ComparisonOperator.EQUALS, value) // <6>
		);
	}
}
// end::custom-base-class-for-repositories[]
