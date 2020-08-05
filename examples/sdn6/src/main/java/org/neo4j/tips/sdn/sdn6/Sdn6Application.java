package org.neo4j.tips.sdn.sdn6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

@SpringBootApplication
public class Sdn6Application {

	/**
	 * Makes all declarative transactions on the service level reactive.
	 */
	@Component
	static class ReactiveTransactionManagementConfigurer implements TransactionManagementConfigurer {

		private final ReactiveTransactionManager reactiveTransactionManager;

		public ReactiveTransactionManagementConfigurer(ReactiveTransactionManager reactiveTransactionManager) {
			this.reactiveTransactionManager = reactiveTransactionManager;
		}

		@Override
		public TransactionManager annotationDrivenTransactionManager() {
			return reactiveTransactionManager;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(Sdn6Application.class, args);
	}
}
