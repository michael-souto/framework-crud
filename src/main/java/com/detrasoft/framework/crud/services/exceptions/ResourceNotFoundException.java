package com.detrasoft.framework.crud.services.exceptions;

public class ResourceNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(Long idResource) {
		super(String.format("Resource %n not found", idResource));
	}

	public ResourceNotFoundException(String idResource) {
		super(String.format("Resource %s not found", idResource));
	}
}
