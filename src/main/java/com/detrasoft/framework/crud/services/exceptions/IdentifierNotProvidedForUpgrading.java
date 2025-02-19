package com.detrasoft.framework.crud.services.exceptions;

import java.util.UUID;

public class IdentifierNotProvidedForUpgrading extends RuntimeException {
	public IdentifierNotProvidedForUpgrading(UUID idResource) {
		super(String.format("Id %n not informed", idResource));
	}
}
