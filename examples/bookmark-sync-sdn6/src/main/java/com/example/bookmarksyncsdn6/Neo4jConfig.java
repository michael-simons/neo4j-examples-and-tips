package com.example.bookmarksyncsdn6;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jBookmarkManager;
import org.springframework.data.neo4j.core.transaction.Neo4jBookmarksUpdatedEvent;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A configuration for a Neo4j cluster synchronizing Bookmarks between instances via Redis.
 * Redis repositories themselves have been turned off with {@code spring.data.redis.repositories.enabled=false}.
 */
@Configuration(proxyBeanMethods = false)
public class Neo4jConfig {

	private static final String TOPIC_NAME = "neo4j-bookmark-exchange";

	/**
	 * Creates a bean listening on internal Neo4j bookmark events and serializes all of them into a set of strings, which
	 * are than passed to an AMPQ template. This might as well be a Kafka or similar template
	 *
	 * @param messageTemplate
	 * @return A listener for Neo4j bookmarks.
	 */
	@Bean
	public ApplicationListener<Neo4jBookmarksUpdatedEvent> bookmarkListener(
		RedisTemplate<String, Object> messageTemplate) {
		return event -> {
			Set<String> latestBookmarks = event.getBookmarks().stream()
				.flatMap(bookmark -> bookmark.values().stream())
				.collect(Collectors.toSet());

			messageTemplate.convertAndSend(TOPIC_NAME, latestBookmarks);
		};
	}

	/**
	 * A component that listens on a topic and just takes what comes in the topic and use it as new bookmarks.
	 */
	@Component
	static class BookmarkSupplier implements Supplier<Set<Bookmark>> {

		private Set<Bookmark> currentBookmarks = Set.of();

		@SuppressWarnings("unused")
		public void receiveNewBookmarks(Set<String> bookmarks) {

			LoggerFactory.getLogger(BookmarkSupplier.class).info("Received new bookmarks {}", bookmarks);
			currentBookmarks = Set.of(Bookmark.from(bookmarks));
		}

		@Override
		public Set<Bookmark> get() {
			return currentBookmarks;
		}
	}

	@Bean
	RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

		var redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

	@Bean
	MessageListenerAdapter bookmarksReceivedAdapter(BookmarkSupplier receiver) {

		var messageListenerAdapter = new MessageListenerAdapter(receiver, "receiveNewBookmarks");
		messageListenerAdapter.setSerializer(new JdkSerializationRedisSerializer());

		return messageListenerAdapter;
	}

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
		MessageListenerAdapter bookmarksReceivedAdapter) {

		var container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(bookmarksReceivedAdapter, new PatternTopic(TOPIC_NAME));

		return container;
	}

	/**
	 * Plug everything together.
	 *
	 * @param driver
	 * @param databaseNameProvider
	 * @param bookmarkSupplier
	 * @return
	 */
	@Bean
	public PlatformTransactionManager transactionManager(
		Driver driver, DatabaseSelectionProvider databaseNameProvider,
		BookmarkSupplier bookmarkSupplier
	) {
		var bookmarkManager = Neo4jBookmarkManager.create(bookmarkSupplier);
		return new Neo4jTransactionManager(driver, databaseNameProvider, bookmarkManager);
	}
}
