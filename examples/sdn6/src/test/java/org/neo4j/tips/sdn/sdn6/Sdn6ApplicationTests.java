package org.neo4j.tips.sdn.sdn6;

import static org.assertj.core.api.Assertions.*;

import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;

@SpringBootTest
class Sdn6ApplicationTests {

	@Autowired
	private Sdn6ShowcaseService showcaseService;

	@Test
	void imperativeProcedureCallViaDriverShouldWork() {

		var dbName = showcaseService.callSimpleProcedureViaDriverImperative();
		assertThat(dbName).isEqualTo("neo4j");
	}

	@Test
	void reactiveProcedureCallViaDriverShouldWork() {

		showcaseService.callSimpleProcedureViaDriverReactive()
			.as(StepVerifier::create)
			.expectNext("neo4j")
			.expectComplete()
			.verify();
	}

	@Test
	void reactiveProcedureCallViaClientShouldWork() {

		showcaseService.callSimpleProcedureViaClientInsideSpringTransaction()
			.as(StepVerifier::create)
			.expectNext("system")
			.expectComplete()
			.verify();
	}

	@Test
	void findEntitiesViaTemplateShouldWork() {

		showcaseService.findEntitiesViaTemplate(".*Bill.*")
			.map(Person::getId)
			.as(StepVerifier::create)
			.expectNextCount(3)
			.expectComplete()
			.verify();
	}

	@Test
	void saveEntityViaTemplateShouldWork() {

		var random = ThreadLocalRandom.current();

		showcaseService.saveEntityViaTemplate(
			new Person("RandomName" + Float.toHexString(random.nextFloat()), random.nextInt(2000)))
			.as(StepVerifier::create)
			.assertNext(p -> assertThat(p.getId()).isNotNull())
			.expectComplete()
			.verify();
	}

	@Test
	void findEntitiesViaRepositoryShouldWork() {
		TransactionalOperator.create(transactionManager).transactional(
		showcaseService.findEntitiesViaRepository(".*Matrix.*")

			.doOnNext(m -> {
				System.out.println(m.getTitle() + " was directed by " + m.getDirectors().stream().map(Person::getName)
					.collect(Collectors.joining(", ")));
				System.out.println("Staring ");
				m.getActorsAndRoles().forEach((p, r) -> {
					System.out.println(p.getName() + " " + r.getRoles());
				});
			}))
			.as(StepVerifier::create)
			.expectNextCount(3)
			.verifyComplete();
	}

	@Test
	void saveEntityViaRepositoryShouldWork() {

		Movie legend = new Movie("Legend", "n/a");
		Person brian = new Person("Brian Helgeland", 1961);
		Person tom = new Person("Tom Hardy", 1977);

		legend.getDirectors().add(brian);
		legend.getActorsAndRoles().put(tom, new Roles(List.of("Ronald Kray", "Reginald Kray")));

		showcaseService
			.saveEntityViaRepository(legend)
			.map(Movie::getId)
			.flatMapMany(showcaseService::selectArbitraryThingsViaClient)
			.as(StepVerifier::create)
			.expectNext("Reginald Kray", "Ronald Kray")
			.verifyComplete();
	}

	// That test may fail with the current snapshot as of August 4th.
	@Test
	void customQueryWithApocVnodesShouldWork() {

		showcaseService.findVirtualMovieViaApoc()
			.as(StepVerifier::create)
			.assertNext(m -> {
				assertThat(m.getTitle()).matches("Matrix 4.1");
				assertThat(m.getActorsAndRoles()).hasSize(1);
				assertThat(m.getActorsAndRoles().entrySet()).first().satisfies(entry -> {
					assertThat(entry.getKey().getName()).isEqualTo("Emil Eifrem");
					assertThat(entry.getValue().getRoles()).containsExactly("himself");
				});
			})
			.verifyComplete();
	}

	@Test
	void findByExampleShouldWork() {

		showcaseService.findByExample(new Movie("Matrix", null))
			.as(StepVerifier::create)
			.expectNextCount(3)
			.verifyComplete();
	}
}
