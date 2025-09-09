package com.detrasoft.framework.crud.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchField {
	private String label;
	private String field;
	private String columnName;
	private String where;
	private boolean hidden;

	private String subfield;
	private FieldType type;
	private Object value;
}
