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
package org.neo4j.tips.sdn.use_dynamic_finder;

import org.neo4j.tips.sdn.use_dynamic_finder.domain.Thing;
import org.neo4j.tips.sdn.use_dynamic_finder.domain.ThingRepository;
import org.neo4j.tips.sdn.use_dynamic_finder.support.Neo4jRepositoryWithDynamicFinderImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * @author Michael J. Simons
 */
@SpringBootApplication
@EnableNeo4jRepositories(repositoryBaseClass = Neo4jRepositoryWithDynamicFinderImpl.class)
public class UseDynamicFinderApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(UseDynamicFinderApplication.class, args).close();
	}

	private final ThingRepository thingRepository;

	public UseDynamicFinderApplication(ThingRepository thingRepository) {
		this.thingRepository = thingRepository;
	}

	@Override public void run(String... args) throws Exception {
		this.thingRepository.save(new Thing("Thing 1", "This is the 1st thing", 9.99));
		this.thingRepository.save(new Thing("AnotherThing", "This is a thing, too", 9.99));
		this.thingRepository.save(new Thing("YetAnotherThing", "This is a thing, too", 9.99));

		this.thingRepository.findAllByPropertyValue("description", "This is a thing, too")
			.forEach(it -> System.out.println(it.getName()));
	}
}
