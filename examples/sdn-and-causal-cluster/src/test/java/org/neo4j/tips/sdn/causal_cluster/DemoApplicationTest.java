/*
 * Copyright (c) 2019 "Neo4j,"
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
package org.neo4j.tips.sdn.causal_cluster;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.neo4j.junit.jupiter.causal_cluster.NeedsCausalCluster;
import org.neo4j.junit.jupiter.causal_cluster.Neo4jUri;
import org.neo4j.tips.sdn.causal_cluster.domain.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Michael J. Simons
 */
// tag::testing-cc-support[]
@NeedsCausalCluster // <1>
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // <2>
@ContextConfiguration(initializers = { DemoApplicationTest.Initializer.class }) // <3>
class DemoApplicationTest {

	@Neo4jUri // <4>
	private static String clusterUri;

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
				"spring.data.neo4j.uri=" + clusterUri, // <5>
				"spring.data.neo4j.username=neo4j",
				"spring.data.neo4j.password=password"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Autowired
	TestRestTemplate restTemplate; // <6>

	@RepeatedTest(10)
	void useBookmarkUnderLoad() throws InterruptedException {

		int numberOfRequests = 10;

		Callable<Thing> callableRequest = () -> {
			ResponseEntity<Thing> response = restTemplate
				.postForEntity("/new", new HttpEntity<Void>(null, null), Thing.class);
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new RuntimeException(response.getStatusCode().toString());
			}
			return response.getBody();
		};

		ExecutorService executor = Executors.newCachedThreadPool();
		List<Future<Thing>> calledRequests = executor.invokeAll(IntStream.range(0, numberOfRequests)
			.mapToObj(i -> callableRequest).collect(toList()));
		try {
			calledRequests.forEach(request -> {
				try {
					request.get();
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					Assertions.fail("At least one request failed " + e.getMessage());
				}
			});
		} finally {
			executor.shutdown();
		}
	}
}
// end::testing-cc-support[]