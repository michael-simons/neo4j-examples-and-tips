package org.neo4j.tips.micronaut;

import io.micronaut.context.annotation.Factory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;

import javax.inject.Singleton;

import org.neo4j.configuration.connectors.HttpConnector;
import org.neo4j.configuration.connectors.HttpsConnector;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.kernel.api.procedure.GlobalProcedures;
import org.neo4j.kernel.extension.ExtensionFactory;
import org.neo4j.kernel.extension.context.ExtensionContext;
import org.neo4j.kernel.lifecycle.Lifecycle;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;

@Factory
public class Neo4jFactory {

	/**
	 * Collecting your stored procedures etc.
	 * The scanning for user functions only works when they are run as plugin on a standalone instance.
	 */
	private static class GDSExtensions extends ExtensionFactory<GDSExtensions.Dependencies> {
		interface Dependencies {
			GlobalProcedures procedures();
		}

		final Set<Class<?>> procedures;
		final Set<Class<?>> functions;

		public GDSExtensions(Set<Class<?>> procedures, Set<Class<?>> functions) {
			super("gds");

			this.procedures = procedures;
			this.functions = functions;
		}

		@Override
		public Lifecycle newInstance(ExtensionContext context, Dependencies dependencies) {
			return new LifecycleAdapter() {
				@Override
				public void start() throws Exception {
					var globalProcedures = dependencies.procedures();
					for (Class<?> procedure : procedures) {
						globalProcedures.registerProcedure(procedure);
					}
					for (Class<?> function : functions) {
						globalProcedures.registerFunction(function);
					}

					// Aggregation function missing
				}
			};
		}
	}

	@Singleton
	DatabaseManagementService databaseManagementService() throws IOException {

		var gdsExtensions = new GDSExtensions(Collections.emptySet(), Set.of(SomeCustomFunction.class));
		// This creates an extended version of the DatabaseManagementServiceBuilder and lets you add
		// your extension to it (in the non-static initializer block)
		return new DatabaseManagementServiceBuilder(
			Files.createTempDirectory("neo4j").toFile()) {
			{
				extensions.add(gdsExtensions);
			}
		}
			// You don't want to have neo4js http connectors
			.setConfig(HttpConnector.enabled, false)
			.setConfig(HttpsConnector.enabled, false)
			.build();
	}
}
