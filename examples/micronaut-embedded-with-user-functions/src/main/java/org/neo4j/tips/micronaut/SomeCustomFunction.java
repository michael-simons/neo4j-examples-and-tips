package org.neo4j.tips.micronaut;

import org.neo4j.procedure.UserFunction;

public class SomeCustomFunction {

	@UserFunction("hello.micronaut")
	public String apply() {

		return "Hello, World.\n";
	}
}
