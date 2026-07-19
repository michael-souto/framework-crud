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
	private boolean principal;
	private boolean key;

	private String subfield;
	private FieldType type;
	private Object value;

	public SearchField(String header, String field, String subfield, FieldType type, String columnName, String where) {
		this.label = header;
		this.field = field;
		this.subfield = subfield;
		this.type = type;
		this.columnName = columnName;
		this.where = where;
	}

	public String getHeader() {
		return label;
	}

	public void setHeader(String header) {
		this.label = header;
	}
}
