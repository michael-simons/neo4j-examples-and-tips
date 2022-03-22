package org.neo4j.examples.neo4j_from_the_jvm_ecoysystem.springdataasync;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
public class Movie {

	@Id @GeneratedValue
	Long id;

	String title;

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override public String toString() {
		return "Movie{" +
			   "id=" + id +
			   ", title='" + title + '\'' +
			   '}';
	}
}
