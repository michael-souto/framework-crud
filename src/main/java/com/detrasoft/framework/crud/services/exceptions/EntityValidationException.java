package com.detrasoft.framework.crud.services.exceptions;

public class EntityValidationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EntityValidationException(String msg) {
		super(msg);
	}

}
