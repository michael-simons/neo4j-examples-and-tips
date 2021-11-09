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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michael J. Simons
 */
@RestController
@RequestMapping("/api/people")
public class PeopleController {

	private final PeopleRepository peopleRepository;

	public PeopleController(PeopleRepository peopleRepository) {
		this.peopleRepository = peopleRepository;
	}

	@PostMapping
	@ResponseStatus(value = HttpStatus.CREATED)
	Person createNewPerson(@RequestBody Person person) {

		return peopleRepository.save(person);
	}
}
