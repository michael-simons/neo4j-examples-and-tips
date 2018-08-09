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
// tag::enable-custom-base-class-for-repositories[]
@SpringBootApplication
@EnableNeo4jRepositories(repositoryBaseClass = Neo4jRepositoryWithDynamicFinderImpl.class)
public class UseDynamicFinderApplication {
	// end::enable-custom-base-class-for-repositories[]

	public static void main(String[] args) {
		SpringApplication.run(UseDynamicFinderApplication.class, args).close();
	}

	// tag::enable-custom-base-class-for-repositories[]
}
// end::enable-custom-base-class-for-repositories[]
