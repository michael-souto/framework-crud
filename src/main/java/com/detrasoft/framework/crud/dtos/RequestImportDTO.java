package com.detrasoft.framework.crud.dtos;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestImportDTO<T> {

	private Operation operation;
	private List<T> data;
	private List<String> keys;
	private UUID groupFieldDefinitionId;
	private UUID complementFieldId;
	private String complementFieldText;
	private String complementFieldName;
	private String complementFieldLabel;
	private Object complementFieldValue;
	
	public enum Operation {
		CREATE, UPDATE, CREATE_OR_UPDATE
	}
}