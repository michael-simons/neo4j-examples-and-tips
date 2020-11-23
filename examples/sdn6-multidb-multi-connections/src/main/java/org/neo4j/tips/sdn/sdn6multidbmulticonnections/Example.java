package org.neo4j.tips.sdn.sdn6multidbmulticonnections;

import java.util.function.BiFunction;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.types.TypeSystem;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness.Whatever;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.fitness.WhateverRepository;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies.Movie;
import org.neo4j.tips.sdn.sdn6multidbmulticonnections.movies.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.data.neo4j.core.PreparedQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Database config used:
 * create user u_movies set PASSWORD "p_movies" change not required;
 *
 * create database movies
 *
 * CREATE ROLE movies_publisher AS COPY OF publisher;
 * REVOKE GRANT ACCESS ON DATABASES * FROM movies_publisher;
 * GRANT ACCESS ON DATABASE movies TO movies_publisher;
 * GRANT ROLE movies_publisher TO u_movies;
 *
 *
 * create user u_fitness set PASSWORD "p_fitness" change not required;
 *
 * create database fitness
 *
 * CREATE ROLE fitness_publisher AS COPY OF publisher;
 * REVOKE GRANT ACCESS ON DATABASES * FROM fitness_publisher;
 * GRANT ACCESS ON DATABASE fitness TO fitness_publisher;
 * GRANT ROLE fitness_publisher TO u_fitness;
 * ```
 */
@Component
public class Example implements CommandLineRunner {

	@Autowired
	private MovieRepository movieRepository;

	@Autowired
	private WhateverRepository whateverRepository;

	@Autowired @Qualifier("moviesDriver")
	private Driver moviesDriver;

	@Autowired @Qualifier("fitnessDriver")
	private Driver fitnessDriver;

	@Autowired @Qualifier("moviesClient")
	private Neo4jClient moviesClient;

	@Autowired @Qualifier("fitnessClient")
	private Neo4jClient fitnessClient;

	@Autowired @Qualifier("moviesManager")
	private PlatformTransactionManager moviesManager;

	@Autowired @Qualifier("fitnessManager")
	private PlatformTransactionManager fitnessManager;

	@Autowired @Qualifier("moviesTemplate")
	private Neo4jTemplate moviesTemplate;

	@Autowired @Qualifier("fitnessTemplate")
	private Neo4jTemplate fitnessTemplate;

	@Override
	public void run(String... args) throws Exception {
		movieRepository.save(new Movie("A Test", "A Tagline"));
		movieRepository.findById("A Test")
			.ifPresentOrElse(m -> System.out.println("Found " + m), () -> System.out.println("Found nothing"));

		var id = whateverRepository.save(new Whatever("I don't know what this is.")).getId();
		whateverRepository.findById(id)
			.ifPresentOrElse(m -> System.out.println("Found " + m), () -> System.out.println("Found nothing"));

		// Using the wrong _driver_ (that does not know a static concept of database, only for the session)
		// will fail for the given database
		var dbNameQuery = "call db.info() yield name";

		String db;
		db = moviesDriver.session(SessionConfig.builder().withDatabase("movies").build()).readTransaction(tx -> tx.run(
			dbNameQuery).single().get("name")).asString();
		System.out.println(db);

		db = fitnessDriver.session(SessionConfig.builder().withDatabase("fitness").build())
			.readTransaction(tx -> tx.run(
				dbNameQuery).single().get("name")).asString();
		System.out.println(db);

		// Same with the client OUT SIDE A TRANSACTION!
		// If you omit it, it goes to the default db
		db = moviesClient.query(dbNameQuery).in("movies").fetchAs(String.class).one().get();
		System.out.println(db);

		db = fitnessClient.query(dbNameQuery).in("fitness").fetchAs(String.class).one().get();
		System.out.println(db);

		// Inside a transaction, it _must_ match the one of the tx manager
		db = new TransactionTemplate(moviesManager)
			.execute(t -> moviesClient.query(dbNameQuery).in("movies").fetchAs(String.class).one().get());
		System.out.println(db);
		db = new TransactionTemplate(fitnessManager)
			.execute(t -> fitnessClient.query(dbNameQuery).in("fitness").fetchAs(String.class).one().get());
		System.out.println(db);

		// Templates do all this above automatically.
		var preparedQuery = PreparedQuery.queryFor(String.class)
			.withCypherQuery(dbNameQuery)
			.usingMappingFunction((TypeSystem t, Record r) -> r.get("name").asString())
			.build();

		db = moviesTemplate.toExecutableQuery(preparedQuery).getSingleResult().get();
		System.out.println(db);

		db = fitnessTemplate.toExecutableQuery(preparedQuery).getSingleResult().get();
		System.out.println(db);
	}
}
