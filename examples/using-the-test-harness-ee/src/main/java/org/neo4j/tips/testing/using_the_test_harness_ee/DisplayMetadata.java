package org.neo4j.tips.testing.using_the_test_harness_ee;

import static java.util.stream.Collectors.*;

import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.impl.api.KernelTransactionImplementation;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.UserFunction;

public class DisplayMetadata {

	@Context
	public GraphDatabaseService db;

	@Context public KernelTransaction currentTransaction;

	@UserFunction("examples.mirrorMeta")
	public String apply() {

		/* Official way
		Result result = db.execute("CALL dbms.getTXMetaData()");
		Map<String, Object> metaData = (Map<String, Object>) result.next().get("metadata");
*/

		Map<String, Object> metaData = ((KernelTransactionImplementation) currentTransaction).getMetaData();
		return metaData.entrySet().stream().map(e -> e.getKey() + " = " + e.getValue()).collect(joining(", "));
	}
}
