package org.neo4j.tips.cluster.sdn_ogm;

import io.github.resilience4j.retry.RetryRegistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.log.LogMessage;

@SpringBootApplication
public class Application {

	private static final Log log = LogFactory.getLog(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public Application(RetryRegistry retryRegistry) {
		retryRegistry.retry("neo4j").getEventPublisher().onRetry(e -> log.warn(LogMessage.format("Retrying %s", e)));
	}
}
