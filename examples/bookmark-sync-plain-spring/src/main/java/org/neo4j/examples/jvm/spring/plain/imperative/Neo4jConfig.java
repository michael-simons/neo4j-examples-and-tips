package org.neo4j.examples.jvm.spring.plain.imperative;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * A configuration for a Neo4j cluster synchronizing Bookmarks between instances via Redis.
 * Redis repositories themselves have been turned off with {@code spring.data.redis.repositories.enabled=false}.
 */
@Configuration
public class Neo4jConfig {

	@Bean
	RedisSerializer<Object> bookmarkSerializer() {
		return new RedisSerializer<>() {

			@Override
			public byte[] serialize(Object o) throws SerializationException {
				var bookmark = (Bookmark) o;
				var v = String.join(",", bookmark.values());
				return v.getBytes(StandardCharsets.UTF_8);
			}

			@Override
			public Object deserialize(byte[] bytes) throws SerializationException {
				var values = Arrays.stream(new String(bytes, StandardCharsets.UTF_8).split(","))
					.collect(Collectors.toSet());
				return Bookmark.from(values);
			}
		};
	}

	@Bean
	RedisTemplate<Object, Object> redisBookmarkTemplate(RedisConnectionFactory c, RedisSerializer<Object> bookmarkSerializer) {

		var redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(c);
		redisTemplate.setValueSerializer(bookmarkSerializer);
		return redisTemplate;
	}

	@Bean
	Neo4jBookmarkManager bookmarkManager(Driver driver, RedisTemplate<Object, Object> redisBookmarkTemplate) {
		return new Neo4jBookmarkManager(driver, redisBookmarkTemplate);
	}

	/**
	 * Here an adapter is created between Redis and the BookmarkManager
	 */
	@Bean
	MessageListenerAdapter bookmarksReceivedAdapter(Neo4jBookmarkManager receiver, RedisSerializer<Object> bookmarkSerializer) {

		var messageListenerAdapter = new MessageListenerAdapter(receiver, "receiveNewBookmarks");
		messageListenerAdapter.setSerializer(bookmarkSerializer);

		return messageListenerAdapter;
	}

	/**
	 * This is boilerplate for setting up the adapter from above to listen on the given topic.
	 */
	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
		MessageListenerAdapter bookmarksReceivedAdapter) {

		var container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(bookmarksReceivedAdapter, new PatternTopic(Neo4jBookmarkManager.TOPIC_NAME));

		return container;
	}
}
