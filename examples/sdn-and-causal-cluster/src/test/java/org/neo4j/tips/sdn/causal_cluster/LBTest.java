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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.junit.jupiter.causal_cluster.CausalCluster;
import org.neo4j.junit.jupiter.causal_cluster.NeedsCausalCluster;
import org.neo4j.junit.jupiter.causal_cluster.Neo4jCluster;
import org.neo4j.junit.jupiter.causal_cluster.Neo4jServer;
import org.neo4j.tips.sdn.causal_cluster.domain.Thing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * @author Michael J. Simons
 */
@NeedsCausalCluster(numberOfCoreServers = 5)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LBTest {

	@CausalCluster
	private static Neo4jCluster theCluster;

	private static final long sequenceNumber = -1L;
	private static Long thingId;

	@DynamicPropertySource
	static void neo4jProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.neo4j.uri", theCluster::getURI);
		registry.add("spring.data.neo4j.username", () -> "neo4j");
		registry.add("spring.data.neo4j.password", () -> "password");
	}

	private final static Log log = LogFactory.getLog(LBTest.class);

	@Autowired
	TestRestTemplate restTemplate;

	@Autowired
	private RetryRegistry retry;

	// Done outside the spring context on purpose
	@BeforeAll
	static void setupAThing() {

		Value parameters = Values.parameters("sequenceNumber", sequenceNumber, "name", "generated");

		try (Driver driver = GraphDatabase.driver(theCluster.getURI(), AuthTokens.basic("neo4j", "password"));
			Session session = driver.session()) {

			TransactionWork<Long> createThing = tx -> tx
				.run("CREATE (t:Thing {sequenceNumber: $sequenceNumber, name: $name}) RETURN id(t) as id", parameters)
				.single().get("id")
				.asLong();
			thingId = session.writeTransaction(createThing);
		}
	}

	@Test
	void useBookmarkUnderLoad() throws InterruptedException {

		int numberOfParallelRequests = 16;
		for (int i = 0; i < 50; ++i) {
			Callable<Thing> callableRequest = () -> {
				ResponseEntity<Thing> response = restTemplate
					.getForEntity("/get/{sequence}", Thing.class, sequenceNumber);
				if (response.getStatusCode() != HttpStatus.OK) {
					throw new RuntimeException(response.getStatusCode().toString());
				}
				return response.getBody();
			};

			ExecutorService executor = Executors.newCachedThreadPool();
			List<Future<Thing>> calledRequests = executor.invokeAll(IntStream.range(0, numberOfParallelRequests)
				.mapToObj(ignored -> callableRequest).collect(toList()));
			try {
				calledRequests.forEach(request -> {
					try {
						Thing t = request.get();
						assertThat(t.getId()).isEqualTo(thingId);
					} catch (InterruptedException e) {
					} catch (ExecutionException e) {
						log.error("Request failed: " + e.getMessage());
					}
				});
			} finally {
				executor.shutdown();
			}
			Thread.sleep(100);
		}

		Retry.Metrics neo4jBackend = retry.retry("neo4j").getMetrics();
		log.info("Metrics: ");
		log.info(
			"Successful calls (with/without retry) " + neo4jBackend.getNumberOfSuccessfulCallsWithRetryAttempt() + ", "
				+ neo4jBackend.getNumberOfSuccessfulCallsWithoutRetryAttempt());
		log.info("Failed calls (with/without retry) " + neo4jBackend.getNumberOfFailedCallsWithRetryAttempt() + ", "
			+ neo4jBackend.getNumberOfFailedCallsWithoutRetryAttempt());
	}

	@AfterAll
	static void checkQueryLog() {

		int cnt = 0;
		Set<Neo4jServer> runningServers = theCluster.getAllServers().stream()
			.filter(Neo4jServer::isContainerRunning).collect(Collectors.toSet());

		for (Neo4jServer server : runningServers) {
			if (server.getQueryLog()
				.contains("MATCH (n:`Thing`) WHERE n.`sequenceNumber` = $`sequenceNumber_0` WITH n RETURN n, ID(n)")) {
				++cnt;
			}
		}
		if (cnt < runningServers.size() / 2) {
			fail("Only " + cnt + " servers of a cluster with " + runningServers.size() + "servers have been used");
		}
		log.info(cnt + " of " + runningServers.size() + " have been queried.");
	}
}
// end::testing-cc-support[]