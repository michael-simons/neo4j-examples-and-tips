package org.neo4j.tips.sdn.sdn6.movies;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
