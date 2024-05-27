package com.detrasoft.framework.crud.services.exceptions;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(UUID idResource) {
		super(String.format("Resource %n not found", idResource));
	}

	public ResourceNotFoundException(String idResource) {
		super(String.format("Resource %s not found", idResource));
	}
}
