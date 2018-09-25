/*
 * Copyright (c) 2018 "Neo4j, Inc." <https://neo4j.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.tips.sdn.validate_transaction_settings;

// tag::provide-validating-transaction-manager[]
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

// end::provide-validating-transaction-manager[]

/**
 * @author Michael J. Simons
 */
@Profile("plain-sdn-scenario")
// tag::provide-validating-transaction-manager[]
@Configuration
class TransactionManagerConfigurationPlainSDN {

	private final SessionFactory sessionFactory;

	public TransactionManagerConfigurationPlainSDN(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		Neo4jTransactionManager transactionManager = new Neo4jTransactionManager(sessionFactory);
		transactionManager.setValidateExistingTransaction(true);
		return transactionManager;
	}
}
// end::provide-validating-transaction-manager[]
