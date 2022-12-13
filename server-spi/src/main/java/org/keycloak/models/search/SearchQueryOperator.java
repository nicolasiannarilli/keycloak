package org.keycloak.models.search;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SearchQueryOperator {
	AND,
	EQUALS,
	IN,
	LIKE,
	NOT,
	OR,
	GT,
	GTE,
	LT,
	LTE;
	
	@JsonValue
	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}