package org.neo4j.examples.jvm.spring.plain.imperative;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.neo4j.driver.Bookmark;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class Neo4jBookmarkManager {

	public static final String TOPIC_NAME = "neo4j-bookmark-exchange";

	private final Driver driver;
	private final RedisTemplate<Object, Object> messageTemplate;

	private final Set<Bookmark> bookmarks = new HashSet<>();

	private final Lock readLock;
	private final Lock writeLock;

	public Neo4jBookmarkManager(Driver driver, RedisTemplate<Object, Object> messageTemplate) {

		this.driver = driver;
		this.messageTemplate = messageTemplate;

		var reentrantReadWriteLock = new ReentrantReadWriteLock();
		this.readLock = reentrantReadWriteLock.readLock();
		this.writeLock = reentrantReadWriteLock.writeLock();

	}

	public <T> T doWithBookmarks(Function<Session, T> callable) {
		return doWithBookmarks(UnaryOperator.identity(), callable);
	}

	public <T> T doWithBookmarks(UnaryOperator<SessionConfig.Builder> customizer, Function<Session, T> callable) {

		var bookmarks = getBookmarks();
		var config = customizer.apply(SessionConfig.builder()).withBookmarks(bookmarks).build();
		try (var session = driver.session(config)) {

			T result = callable.apply(session);
			this.storeBookmark(session.lastBookmark(), bookmarks);
			return result;
		}
	}

	public Collection<Bookmark> getBookmarks() {
		try {
			readLock.lock();
			return Set.copyOf(this.bookmarks);
		} finally {
			readLock.unlock();
		}
	}

	public void storeBookmark(Bookmark bookmark, Collection<Bookmark> usedBookmarks) {

		try {
			writeLock.lock();
			bookmarks.removeAll(usedBookmarks);
			bookmarks.add(bookmark);

			messageTemplate.convertAndSend(TOPIC_NAME, bookmark);
		} finally {
			writeLock.unlock();
		}
	}

	public void receiveNewBookmarks(Bookmark newBookmark) {

		LoggerFactory.getLogger(Neo4jBookmarkManager.class).info("Received new bookmarks {}", newBookmark);
		try {
			writeLock.lock();
			this.bookmarks.add(newBookmark);
		} finally {
			writeLock.unlock();
		}
	}
}
