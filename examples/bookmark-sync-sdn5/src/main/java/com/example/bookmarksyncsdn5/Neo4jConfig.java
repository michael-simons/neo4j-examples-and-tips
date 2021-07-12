package com.example.bookmarksyncsdn5;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.bookmark.BookmarkManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * A configuration for a Neo4j cluster synchronizing Bookmarks between instances via Redis.
 * Redis repositories themselves have been turned off with {@code spring.data.redis.repositories.enabled=false}.
 */
@Configuration(proxyBeanMethods = false)
public class Neo4jConfig {

	private static final String TOPIC_NAME = "neo4j-bookmark-exchange";

	@Component
	static class PublishingBookmarkManager implements BookmarkManager {

		private final RedisTemplate<String, Object> messageTemplate;

		private final Set<String> bookmarks = new HashSet<>();

		private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
		private final Lock read = lock.readLock();
		private final Lock write = lock.writeLock();

		PublishingBookmarkManager(RedisTemplate<String, Object> messageTemplate) {
			this.messageTemplate = messageTemplate;
		}

		@Override
		public Collection<String> getBookmarks() {
			try {
				read.lock();
				HashSet<String> bookmarksToUse = new HashSet<>(this.bookmarks);
				return Collections.unmodifiableSet(bookmarksToUse);
			} finally {
				read.unlock();
			}
		}

		@Override
		public void storeBookmark(String bookmark, Collection<String> usedBookmarks) {

			try {
				write.lock();
				bookmarks.removeAll(usedBookmarks);
				bookmarks.add(bookmark);

				messageTemplate.convertAndSend(TOPIC_NAME, Collections.singleton(bookmark));
			} finally {
				write.unlock();
			}
		}

		public void receiveNewBookmarks(Set<String> newBookmarks) {

			LoggerFactory.getLogger(PublishingBookmarkManager.class).info("Received new bookmarks {}", newBookmarks);
			try {
				write.lock();
				this.bookmarks.addAll(newBookmarks);
			} finally {
				write.unlock();
			}
		}
	}

	@Bean
	RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

		var redisTemplate = new RedisTemplate<String, Object>();
		redisTemplate.setConnectionFactory(connectionFactory);
		return redisTemplate;
	}

	@Bean
	MessageListenerAdapter bookmarksReceivedAdapter(BookmarkManager receiver) {

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
}
