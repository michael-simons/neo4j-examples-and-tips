package org.neo4j.tips.cluster.sdn_ogm.support;

import java.util.function.Predicate;

import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.exceptions.TransientException;
import org.neo4j.ogm.exception.CypherException;

public class RetryOGMSDNExceptionPredicate implements Predicate<Throwable> {

	@Override
	public boolean test(Throwable throwable) {

		Throwable ex = throwable;
		if (throwable instanceof CypherException) {
			ex = throwable.getCause();
		}

		if (ex instanceof TransientException) {
			String code = ((TransientException) ex).code();
			return !"Neo.TransientError.Transaction.Terminated".equals(code) &&
				!"Neo.TransientError.Transaction.LockClientStopped".equals(code);
		} else {
			return ex instanceof SessionExpiredException || ex instanceof ServiceUnavailableException;
		}
	}
}
