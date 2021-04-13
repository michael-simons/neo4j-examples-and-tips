package org.neo4j.tips.sdn.sdn53withembeddedneo4joverbolt;

import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.springframework.boot.autoconfigure.Neo4jDriverProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration(proxyBeanMethods = false)
public class Neo4jConfig {

	/**
	 * This configuration configures the embedded instance. this is the place where any embedded CC has to go
	 */
	@Configuration(proxyBeanMethods = false)
	static class Neo4jEmbeddedConfig {

		@Bean
		public DatabaseManagementService databaseManagementService() throws IOException {

			// Get a random, free port. We could do this by using new SocketAddress("localhost", 0)
			// but we wouldn't be able to retrieve the port via Neo4j means afterwards (it would indicate 0 as ephemeral port)
			var freePort = SocketUtils.findAvailableTcpPort();

			return new DatabaseManagementServiceBuilder(Files.createTempDirectory("neo4j"))
				.setConfig(BoltConnector.enabled, true)
				.setConfig(BoltConnector.listen_address, new SocketAddress("localhost", freePort))
				.build();
		}
	}

	/**
	 * This configuration is dependent on the one above. We used an epheremal port there so that it won't clash with
	 * any other Neo4j instance. That port is not known upfront, so
	 */
	@Configuration(proxyBeanMethods = false)
	static class Neo4jDriverConfig {

		@Bean
		public Driver driver(Neo4jDriverProperties properties, DatabaseManagementService databaseManagementService) {

			var boltPort = databaseManagementService.database("neo4j")
				.executeTransactionally(
					"CALL dbms.listConfig() yield name, value " +
						"WHERE name = 'dbms.connector.bolt.listen_address' " +
						"RETURN value",
					Map.of(),
					result -> {
						var listenAddress = (String) result.next().get("value");
						var pattern = Pattern.compile("(?:\\w+:)?(\\d+)");
						var matcher = pattern.matcher(listenAddress);
						if (!matcher.matches()) {
							throw new RuntimeException("Could not extract bolt port!");
						}
						return matcher.toMatchResult().group(1);
					}
				);

			// Get everything defined in properties, environment or whatever
			var driverConfiguration = properties.asDriverConfig();
			// And use the port from above
			return GraphDatabase.driver("bolt://localhost:" + boltPort, AuthTokens.none(), driverConfiguration);
		}
	}
}
