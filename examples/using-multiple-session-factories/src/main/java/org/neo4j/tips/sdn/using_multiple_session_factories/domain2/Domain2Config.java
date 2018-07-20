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
package org.neo4j.tips.sdn.using_multiple_session_factories.domain2;

import static org.neo4j.tips.sdn.using_multiple_session_factories.domain2.Domain2Config.*;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;

/**
 * @author Michael J. Simons
 */
@Configuration
@EnableNeo4jRepositories(
		sessionFactoryRef = SESSION_FACTORY,
		transactionManagerRef = TRANSACTION_MANAGER,
		basePackages = BASE_PACKAGE)
public class Domain2Config {
	static final String BASE_PACKAGE = "org.neo4j.tips.sdn.using_multiple_session_factories.domain2";

	static final String SESSION_FACTORY = "sessionFactoryForDomain2";

	static final String TRANSACTION_MANAGER = "transactionManagerForDomain2";

	@Bean
	@ConfigurationProperties("custom-configuration-for-domain2")
	public Neo4jProperties myNeo4jProperties() {
		return new Neo4jProperties();
	}

	@Bean
	public org.neo4j.ogm.config.Configuration ogmConfiguration() {
		return myNeo4jProperties().createConfiguration();
	}

	@Bean(name = SESSION_FACTORY)
	public SessionFactory sessionFactory() {
		return new SessionFactory(ogmConfiguration(), BASE_PACKAGE);
	}

	@Bean(name = TRANSACTION_MANAGER)
	public Neo4jTransactionManager neo4jTransactionManager() {
		return new Neo4jTransactionManager(sessionFactory());
	}
}
