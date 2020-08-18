package org.neo4j.tips.cluster.sdn_ogm.config;

import io.github.resilience4j.retry.RetryRegistry;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.stereotype.Component;

@Component
public class RetryConfig {

	private static final Log log = LogFactory.getLog(RetryConfig.class);

	private final RetryRegistry retryRegistry;

	public RetryConfig(RetryRegistry retryRegistry) {
		this.retryRegistry = retryRegistry;
	}

	@PostConstruct
	void configureLogging() {
		retryRegistry.retry("neo4j").getEventPublisher().onRetry(e -> log.warn(LogMessage.format("Retrying %s", e)));
	}
}
