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
package org.neo4j.tips.testing.testing_ogm_against_embedded_with_apoc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import apoc.ApocConfig;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Michael J. Simons
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

	private static Neo4j neo4j;

	@BeforeAll
	static void startEmbeddedNeo4j() throws URISyntaxException {

		// We need the directory containing the APOC jar, otherwise all APOC procedures must be loaded manually.
		// While the intuitive idea might be not having APOC on the class path at all in that case and just dump
		// it into the plugin directory, it doesn't work as APOC needs some extension factories to work with
		// and those are not loaded from the plugin unless it's part of the original class loader that loaded neo.
		// If you know which methods you're a gonna use, you can configure them manually instead.
		var pluginDirContainingApocJar = new File(
			ApocConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI())
			.getParentFile().toPath();
		neo4j = Neo4jBuilders
			.newInProcessBuilder()
			.withDisabledServer() // We don't need the HTTP endpoint
			.withFixture("CREATE (m:Movie {title: 'Fight Club'}) RETURN m")
			.withConfig(GraphDatabaseSettings.plugin_dir, pluginDirContainingApocJar)
			.withConfig(GraphDatabaseSettings.procedure_unrestricted, List.of("apoc.*"))
			.build();

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
		registry.add("spring.data.neo4j.uri", neo4j::boltURI);
		registry.add("spring.data.neo4j.username", () -> null);
		registry.add("spring.data.neo4j.password", () -> null);
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

	@AfterAll
	static void stopEmbeddedNeo4j() {
		neo4j.close();
	}
}
