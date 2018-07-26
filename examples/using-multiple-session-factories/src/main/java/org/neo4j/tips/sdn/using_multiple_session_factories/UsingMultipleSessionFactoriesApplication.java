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
package org.neo4j.tips.sdn.using_multiple_session_factories;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jProperties;

/**
 * @author Michael J. Simons
 */
@SpringBootApplication
public class UsingMultipleSessionFactoriesApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(UsingMultipleSessionFactoriesApplication.class, args).close();
	}

	private final DemoServiceUsingRepositories demoServiceUsingRepositories;

	private final DemoServiceUsingSession demoServiceUsingSession;

	public UsingMultipleSessionFactoriesApplication(DemoServiceUsingRepositories demoServiceUsingRepositories,
			DemoServiceUsingSession demoServiceUsingSession) {
		this.demoServiceUsingRepositories = demoServiceUsingRepositories;
		this.demoServiceUsingSession = demoServiceUsingSession;
	}

	@Override
	public void run(String... args) throws Exception {
		demoServiceUsingRepositories.createSomeFooBar();
		demoServiceUsingSession.readSomeFooBar();
	}
}
