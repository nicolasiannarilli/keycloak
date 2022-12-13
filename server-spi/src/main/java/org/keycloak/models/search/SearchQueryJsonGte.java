package org.keycloak.models.search;

import static org.keycloak.models.search.SearchQueryOperator.GTE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "operator", "property", "value" })
public class SearchQueryJsonGte extends SearchQueryJson {
	@JsonProperty(required = true)
	private final String property;
	@JsonProperty(required = true)
	private final String value;
	
	@JsonCreator
	public SearchQueryJsonGte(@JsonProperty("property") String property, @JsonProperty("value") String value) {
		super(GTE);
		this.property = property;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}
}