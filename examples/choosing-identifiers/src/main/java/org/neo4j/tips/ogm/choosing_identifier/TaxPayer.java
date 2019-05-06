package org.neo4j.tips.ogm.choosing_identifier;

import java.util.Objects;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class TaxPayer {
	@Id
	private String taxId;

	private String name;

	public TaxPayer(String taxId, String name) {
		this.taxId = taxId;
		this.name = name;
	}

	public String getTaxId() {
		return taxId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TaxPayer))
			return false;
		TaxPayer taxPayer = (TaxPayer) o;
		return Objects.equals(taxId, taxPayer.taxId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(taxId);
	}
}
