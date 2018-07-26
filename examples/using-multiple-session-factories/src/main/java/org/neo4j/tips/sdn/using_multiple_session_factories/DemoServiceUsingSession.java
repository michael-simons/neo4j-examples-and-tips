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

import java.util.Map;

import org.neo4j.ogm.session.Session;
import org.neo4j.tips.sdn.using_multiple_session_factories.domain1.Domain1Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Michael J. Simons
 */
@Service
public class DemoServiceUsingSession {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoServiceUsingSession.class);

	private final Session sessionToNeoInstance1;

	private final Session sessionToNeoInstance2;

	public DemoServiceUsingSession( //
			@Qualifier(Domain1Config.SESSION_FACTORY) Session sessionToNeoInstance1, //
			Session aSessionToInstance2 //
	) {
		this.sessionToNeoInstance1 = sessionToNeoInstance1;
		this.sessionToNeoInstance2 = aSessionToInstance2;
	}

	public void readSomeFooBar() {
		this.sessionToNeoInstance1.query(String.class, "MATCH (n) RETURN n.name", Map.of()).forEach(LOGGER::info);
		this.sessionToNeoInstance2.query(String.class, "MATCH (n) RETURN n.name", Map.of()).forEach(LOGGER::info);
	}
}
