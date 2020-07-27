package org.neo4j.tips.micronaut;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.graphdb.QueryStatistics;
import org.neo4j.graphdb.Transaction;

@Controller
public class Neo4jController {

	private final DatabaseManagementService databaseManagementService;

	public Neo4jController(DatabaseManagementService databaseManagementService) {
		this.databaseManagementService = databaseManagementService;
	}

	// curl -X POST localhost:8080/createSomeData
	@Post("/createSomeData")
	QueryStatistics createSomeData() {
		try (Transaction tx = databaseManagementService.database("neo4j").beginTx()) {
			var result = tx.execute("CREATE (m:Test) RETURN m");
			var queryStatistics = result.getQueryStatistics();
			result.close();
			return queryStatistics;
		}
	}

	// curl localhost:8080/callFunction
	@Get("/callFunction")
	String callFunction() {
		try (Transaction tx = databaseManagementService.database("neo4j").beginTx()) {
			var result = tx.execute("RETURN hello.micronaut() AS message");
			var message = (String) result.next().get("message");
			result.close();
			return message;
		}
	}
}
