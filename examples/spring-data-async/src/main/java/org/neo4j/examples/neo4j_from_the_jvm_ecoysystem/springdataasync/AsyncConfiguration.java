package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

	public static final long ASYNC_TIMEOUT_SECONDS = 90L;

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(500);
		executor.setMaxPoolSize(Integer.MAX_VALUE);
		executor.setQueueCapacity(0);
		executor.setAllowCoreThreadTimeOut(true);
		executor.setKeepAliveSeconds((int) ASYNC_TIMEOUT_SECONDS);

		executor.setThreadNamePrefix("AsyncExecutor-");
		executor.initialize();
		return executor;
	}
}

