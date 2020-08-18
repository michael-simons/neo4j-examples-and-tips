package org.neo4j.tips.cluster.sdn_ogm.support;

import java.util.Set;
import java.util.function.Predicate;

import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.exceptions.SessionExpiredException;
import org.neo4j.driver.exceptions.TransientException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;

public class RetrySDN6ExceptionPredicate implements Predicate<Throwable> {

	private static final Set<String> RETRYABLE_ILLEGAL_STATE_MESSAGES = Set
		.of("Transaction must be open, but has already been closed.",
			"Session must be open, but has already been closed.");

	@Override
	public boolean test(Throwable throwable) {

		if (throwable instanceof IllegalStateException) {
			String msg = throwable.getMessage();
			return RETRYABLE_ILLEGAL_STATE_MESSAGES.contains(msg);
		}

		Throwable ex = throwable;
		if (throwable instanceof TransientDataAccessResourceException) {
			ex = throwable.getCause();
		} else if (throwable instanceof RecoverableDataAccessException) { // With SDN 6.0 RC1 this and the next branch can go away.
			ex = throwable.getCause();
		} else if (throwable instanceof NonTransientDataAccessResourceException) {
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
