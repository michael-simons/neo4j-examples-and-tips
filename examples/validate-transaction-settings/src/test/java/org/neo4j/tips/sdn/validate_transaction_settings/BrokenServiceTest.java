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

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.IllegalTransactionStateException;

/**
 * @author Michael J. Simons
 */
// tag::broken-service-test[]
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BrokenServiceTest {

	private static final Predicate<String> MESSAGE_INDICATES_READ_ONLY_MISMATCH = Pattern
		.compile(".*is not marked as read-only but existing transaction is.*").asPredicate();

	private final BrokenService brokenService;

	@Autowired
	public BrokenServiceTest(BrokenService brokenService) {
		this.brokenService = brokenService;
	}

	@Test
	@DisplayName("Wrong transaction usage should fail")
	void wrongTransactionUsageShouldFail() {
		var caughtException = assertThrows(IllegalTransactionStateException.class,
			() -> brokenService.tryToWriteInReadOnlyTx());
		assertTrue(() -> MESSAGE_INDICATES_READ_ONLY_MISMATCH.test(caughtException.getMessage()));
	}
}
// end::broken-service-test[]
