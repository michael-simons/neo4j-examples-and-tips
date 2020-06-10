/*
 * Copyright (c) 2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
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
package org.neo4j.tips.testing.testing_ogm_against_testcontainers_with_apoc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * @author Michael J. Simons
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

	static Path downloadApoc() {
		var apocJar = Path.of("target", "apoc-all.jar");

		if (!Files.isRegularFile(apocJar)) {
			HttpResponse<Path> response;
			try {
				var apocJarUrl = URI.create(
					"https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/4.0.0.13/apoc-4.0.0.13-all.jar");

				var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
				response = client
					.send(HttpRequest.newBuilder().uri(apocJarUrl).GET().build(), BodyHandlers.ofFile(apocJar));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			if (response.statusCode() != 200) {
				throw new RuntimeException("Could not download apoc.");
			}
		}

		return apocJar;
	}

	@Container
	private static final Neo4jContainer neo4j = new Neo4jContainer<>("neo4j:4.0.5")
		.withCopyFileToContainer(MountableFile.forHostPath(downloadApoc()), "/var/lib/neo4j/plugins/apoc-all.jar")
		.withEnv("NEO4J_dbms_security_procedures_unrestricted", "apoc.*");

	@BeforeAll
	static void createFixture() {

		try (Driver driver = GraphDatabase
			.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", neo4j.getAdminPassword()));
			Session session = driver.session()) {
			session.run("CREATE (m:Movie {title: 'Fight Club'}) RETURN m");
		}
	}

	/**
	 * This is the actual part where you make SDN+OGM aware of the different config and the
	 * point it to the test harness embedded bolt connector.
	 * user name and password are null in that case.
	 * This way of configuring tests works since Spring Boot 2.2.6
	 *
	 * @param registry The target of dynamic configuration
	 */
	@DynamicPropertySource
	static void neo4jProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.neo4j.uri", neo4j::getBoltUrl);
		registry.add("spring.data.neo4j.username", () -> "neo4j");
		registry.add("spring.data.neo4j.password", neo4j::getAdminPassword);
	}

	/**
	 * Calls an endpoint that uses a simple repo.
	 *
	 * @param client
	 * @throws Exception
	 */
	@Test
	void shouldFindMovies(@Autowired MockMvc client) throws Exception {

		client.perform(get("/movies"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].title").value("Fight Club"));
	}

	/**
	 * Calls an endpoint that calls apoc (see {@link ApocVersionContributor}).
	 *
	 * @param client
	 * @throws Exception
	 */
	@Test
	void shouldAddApocVersionToActuator(@Autowired MockMvc client) throws Exception {

		client.perform(get("/actuator/info"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.apocVersion").value("4.0.0.13"));
	}
}
