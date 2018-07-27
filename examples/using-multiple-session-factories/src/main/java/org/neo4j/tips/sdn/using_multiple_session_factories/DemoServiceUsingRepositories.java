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
package org.neo4j.tips.sdn.using_multiple_session_factories;

import org.neo4j.tips.sdn.using_multiple_session_factories.domain1.FooEntity;
import org.neo4j.tips.sdn.using_multiple_session_factories.domain1.FooRepository;
import org.neo4j.tips.sdn.using_multiple_session_factories.domain2.BarEntity;
import org.neo4j.tips.sdn.using_multiple_session_factories.domain2.BarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Michael J. Simons
 */
// tag::repository-usage[]
@Service
public class DemoServiceUsingRepositories {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(DemoServiceUsingRepositories.class);

	private final FooRepository fooRepository;

	private final BarRepository barRepository;

	public DemoServiceUsingRepositories(
		FooRepository fooRepository,
		BarRepository barRepository
	) {

		this.fooRepository = fooRepository;
		this.barRepository = barRepository;
	}

	public void createSomeFooBar() {

		FooEntity fooEntity = fooRepository.save(new FooEntity("This is foo"));
		LOGGER.info("Written foo {} with id {}", fooEntity.getName(), fooEntity.getId());

		BarEntity barEntity = barRepository.save(new BarEntity("This is bar"));
		LOGGER.info("Written bar {} with id {}", barEntity.getName(), barEntity.getId());
	}
}
// end::repository-usage[]
