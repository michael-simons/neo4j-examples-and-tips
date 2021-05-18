/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neo4j.tips.sdn.sdn6multidbmulticonnections.health;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.reactive.RxSession;
import org.neo4j.driver.summary.DatabaseInfo;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.ServerInfo;
import org.reactivestreams.Publisher;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.neo4j.core.DatabaseSelection;
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider;
import org.springframework.util.StringUtils;

public final class DatabaseSelectionAwareNeo4jReactiveHealthIndicator extends AbstractReactiveHealthIndicator {

	private final Driver driver;

	private final ReactiveDatabaseSelectionProvider databaseSelectionProvider;

	public DatabaseSelectionAwareNeo4jReactiveHealthIndicator(Driver driver, ReactiveDatabaseSelectionProvider databaseSelectionProvider) {
		this.driver = driver;
		this.databaseSelectionProvider = databaseSelectionProvider;
	}

	@Override
	protected Mono<Health> doHealthCheck(Health.Builder builder) {
		return databaseSelectionProvider.getDatabaseSelection()
			.map(databaseSelection -> databaseSelection == DatabaseSelection.undecided() ?
				SessionConfig.defaultConfig() :
				SessionConfig.builder().withDatabase(databaseSelection.getValue()).build()
			)
			.flatMap(sessionConfig ->
				Mono.usingWhen(
					Mono.fromSupplier(() -> driver.rxSession(sessionConfig)),
					s -> {
						Publisher<Tuple2<String, ResultSummary>> f = s.readTransaction(tx -> {
							RxResult result = tx.run(
								"CALL dbms.components() YIELD name, edition WHERE name = 'Neo4j Kernel' RETURN edition");
							return Mono.from(result.records()).map((record) -> record.get("edition").asString())
								.zipWhen((edition) -> Mono.from(result.consume()));
						});
						return Mono.fromDirect(f);
					},
					RxSession::close
				)
			).map((result) -> {
				addHealthDetails(builder, result.getT1(), result.getT2());
				return builder.build();
			});
	}

	static void addHealthDetails(Health.Builder builder, String edition, ResultSummary resultSummary) {
		ServerInfo serverInfo = resultSummary.server();
		builder.up().withDetail("server", serverInfo.version() + "@" + serverInfo.address()).withDetail("edition",
			edition);
		DatabaseInfo databaseInfo = resultSummary.database();
		if (StringUtils.hasText(databaseInfo.name())) {
			builder.withDetail("database", databaseInfo.name());
		}
	}
}
