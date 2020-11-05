package ac.simons.neo4j.sdn_on_liberty.domain;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * @author Mark Angrish
 * @author Michael J. Simons
 */
@Node
public class Person {

	@Id
	private final String name;

	private Integer born;

	public Person(Integer born, String name) {
		this.born = born;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Integer getBorn() {
		return born;
	}

	public void setBorn(Integer born) {
		this.born = born;
	}

	@Override
	public String toString() {
		return "Person{" +
			"name='" + name + '\'' +
			", born=" + born +
			'}';
	}
}
