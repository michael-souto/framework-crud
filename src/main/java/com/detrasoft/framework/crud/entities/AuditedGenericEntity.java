package com.detrasoft.framework.crud.entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public abstract class AuditedGenericEntity extends GenericEntity {

	@Embedded
	private Audit audit = new Audit();

	@PrePersist
	public void prePersist() {
		audit.setCreatedAt(Instant.now());
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			audit.setUserCreated(SecurityContextHolder.getContext().getAuthentication().getName());
		}
	}

	@PreUpdate
	public void preUpdate() {
		audit.setUpdatedAt(Instant.now());
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			audit.setUserUpdated(SecurityContextHolder.getContext().getAuthentication().getName());
		}
	}
}
