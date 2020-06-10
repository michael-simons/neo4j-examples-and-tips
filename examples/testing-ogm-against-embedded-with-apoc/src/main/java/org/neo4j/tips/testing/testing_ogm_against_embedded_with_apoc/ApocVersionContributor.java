/*
 * Copyright (c) 2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.tips.testing.testing_ogm_against_embedded_with_apoc;

import java.util.Collections;

import org.neo4j.ogm.session.Session;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * @author Michael J. Simons
 */
@Component
public class ApocVersionContributor implements InfoContributor {

	private final Session ogmSession;

	public ApocVersionContributor(Session ogmSession) {
		this.ogmSession = ogmSession;
	}

	@Override
	public void contribute(Info.Builder builder) {

		var apocVersion = ogmSession.queryForObject(String.class, "RETURN apoc.version()", Collections.emptyMap());
		builder.withDetail("apocVersion", apocVersion);
	}
}
